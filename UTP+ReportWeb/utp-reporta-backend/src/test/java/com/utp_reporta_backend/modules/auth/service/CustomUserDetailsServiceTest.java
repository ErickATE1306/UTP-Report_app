package com.utp_reporta_backend.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private CustomUserDetailsService service;

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("ana");
        usuario.setPassword("cifrada");
        usuario.getRoles().add(new Rol(1L, ERol.ROLE_ADMIN));
        return usuario;
    }

    @Test
    @DisplayName("carga al usuario por username o correo con sus roles")
    void cargaUsuario() {
        when(usuarioRepository.findByUsernameOrCorreo("ana@utp.pe", "ana@utp.pe")).thenReturn(Optional.of(usuario()));

        UserDetails detalles = service.loadUserByUsername("ana@utp.pe");

        assertEquals("ana", detalles.getUsername());
        assertEquals("cifrada", detalles.getPassword());
        assertThat(AuthorityUtils.authorityListToSet(detalles.getAuthorities())).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("refleja si el usuario está deshabilitado")
    void usuarioDeshabilitado() {
        Usuario usuario = usuario();
        usuario.setEnabled(false);
        when(usuarioRepository.findByUsernameOrCorreo("ana", "ana")).thenReturn(Optional.of(usuario));

        assertFalse(service.loadUserByUsername("ana").isEnabled());
    }

    @Test
    @DisplayName("falla si el usuario no existe")
    void usuarioInexistente() {
        when(usuarioRepository.findByUsernameOrCorreo("nadie", "nadie")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nadie"));
    }
}
