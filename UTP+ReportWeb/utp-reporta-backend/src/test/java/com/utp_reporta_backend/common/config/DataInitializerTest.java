package com.utp_reporta_backend.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.tipoincidente.repository.TipoIncidenteRepository;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.repository.RolRepository;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {
    @Mock private RolRepository rolRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TipoIncidenteRepository tipoIncidenteRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private ZonaRepository zonaRepository;
    @InjectMocks private DataInitializer initializer;

    @BeforeEach
    void catalogosYaCargados() {
        lenient().when(rolRepository.count()).thenReturn(4L);
        lenient().when(tipoIncidenteRepository.count()).thenReturn(1L);
        lenient().when(sedeRepository.count()).thenReturn(1L);
        lenient().when(zonaRepository.count()).thenReturn(1L);
        lenient().when(rolRepository.findByNombre(ERol.ROLE_SUPERADMIN)).thenReturn(Optional.of(new Rol()));
    }

    @Test
    void creaElSuperadminConLaContrasenaConfigurada() throws Exception {
        when(usuarioRepository.count()).thenReturn(0L);
        ReflectionTestUtils.setField(initializer, "superadminPassword", "Clave-de-prueba-1");

        initializer.run();

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        String guardada = captor.getValue().getPassword();
        assertThat(guardada).isNotEqualTo("Clave-de-prueba-1");
        assertThat(new BCryptPasswordEncoder().matches("Clave-de-prueba-1", guardada)).isTrue();
    }

    @Test
    void noArrancaSinContrasenaSiHayQueCrearElSuperadmin() {
        when(usuarioRepository.count()).thenReturn(0L);
        ReflectionTestUtils.setField(initializer, "superadminPassword", "  ");

        assertThatThrownBy(() -> initializer.run())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("SUPERADMIN_PASSWORD");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void conUsuariosExistentesNoExigeLaContrasenaNiCreaCuentas() throws Exception {
        when(usuarioRepository.count()).thenReturn(3L);
        ReflectionTestUtils.setField(initializer, "superadminPassword", "");

        initializer.run();

        verify(usuarioRepository, never()).save(any());
    }
}
