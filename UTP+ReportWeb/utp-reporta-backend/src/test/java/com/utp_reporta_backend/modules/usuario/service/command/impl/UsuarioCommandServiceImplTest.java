package com.utp_reporta_backend.modules.usuario.service.command.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.utp_reporta_backend.common.exception.BadRequestException;
import com.utp_reporta_backend.common.exception.ResourceNotFoundException;
import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.model.enums.TipoUsuario;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioCommandServiceImplTest {

    private static final Long ID = 1L;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private ZonaRepository zonaRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UsuarioCommandServiceImpl service;

    private Usuario usuario;

    @BeforeEach
    void preparar() {
        usuario = new Usuario();
        usuario.setId(ID);
        usuario.setUsername("ana");
        usuario.setNombreCompleto("Ana Ruiz");
        usuario.setCorreo("ana@utp.pe");
        usuario.setTelefono("999");
        usuario.setPassword("anterior");
        usuario.getRoles().add(new Rol(1L, ERol.ROLE_USUARIO));

        lenient().when(usuarioRepository.findById(ID)).thenReturn(Optional.of(usuario));
        lenient().when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));
    }

    private Zona zona(String nombre) {
        Zona zona = new Zona();
        zona.setNombre(nombre);
        return zona;
    }

    @Test
    @DisplayName("deleteUsuario elimina la cuenta y confirma el cambio en la base (F10)")
    void eliminaCuenta() {
        service.deleteUsuario(ID);

        verify(usuarioRepository).delete(usuario);
        verify(usuarioRepository).flush();
    }

    @Test
    @DisplayName("deleteUsuario responde no encontrado si el usuario no existe (F10)")
    void eliminaInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteUsuario(99L));
    }

    @Test
    @DisplayName("deleteUsuario no permite eliminar a un superadministrador (F10)")
    void noEliminaSuperadmin() {
        usuario.getRoles().add(new Rol(2L, ERol.ROLE_SUPERADMIN));

        assertThrows(BadRequestException.class, () -> service.deleteUsuario(ID));
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }

    @Test
    @DisplayName("deleteUsuario explica el rechazo si la cuenta tiene reportes (F10)")
    void noEliminaCuentaConReportes() {
        org.mockito.Mockito.doThrow(new DataIntegrityViolationException("fk")).when(usuarioRepository).flush();

        BadRequestException error = assertThrows(BadRequestException.class, () -> service.deleteUsuario(ID));

        assertThat(error.getMessage()).contains("reportes").contains("Desactívala");
    }

    @Test
    @DisplayName("updateUsuarioEnabledStatus cambia el estado y devuelve el DTO")
    void cambiaEstado() {
        UsuarioDTO dto = service.updateUsuarioEnabledStatus(ID, false);

        assertFalse(usuario.isEnabled());
        assertFalse(dto.isEnabled());
        assertEquals("ana", dto.getUsername());
        assertThat(dto.getRoles()).containsExactly("ROLE_USUARIO");
    }

    @Test
    @DisplayName("updateUsuarioEnabledStatus devuelve null si el usuario no existe")
    void cambiaEstadoInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.updateUsuarioEnabledStatus(99L, false));
    }

    @Test
    @DisplayName("B05: updateUsuario responde no encontrado si el usuario no existe")
    void actualizaInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateUsuario(99L, new UsuarioUpdateRequest(), null));
    }

    @Test
    @DisplayName("actualiza solo los campos enviados")
    void actualizaCamposEnviados() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombreCompleto("Ana María");
        request.setTipoUsuario(TipoUsuario.DOCENTE);

        UsuarioDTO dto = service.updateUsuario(ID, request, null);

        assertEquals("Ana María", dto.getNombreCompleto());
        assertEquals(TipoUsuario.DOCENTE, dto.getTipoUsuario());
        assertEquals("ana@utp.pe", dto.getCorreo());
        assertEquals("999", dto.getTelefono());
    }

    @Test
    @DisplayName("rechaza un teléfono que pertenece a otro usuario")
    void telefonoRepetido() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setTelefono("888");
        when(usuarioRepository.existsByTelefonoAndIdNot("888", ID)).thenReturn(true);

        assertNull(service.updateUsuario(ID, request, null));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("acepta un teléfono nuevo y disponible")
    void telefonoDisponible() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setTelefono("888");
        when(usuarioRepository.existsByTelefonoAndIdNot("888", ID)).thenReturn(false);

        assertEquals("888", service.updateUsuario(ID, request, null).getTelefono());
    }

    @Test
    @DisplayName("no valida la unicidad si el teléfono no cambia")
    void telefonoSinCambios() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setTelefono("999");

        service.updateUsuario(ID, request, null);

        verify(usuarioRepository, never()).existsByTelefonoAndIdNot(anyString(), any());
    }

    @Test
    @DisplayName("cifra la contraseña si se envía una nueva")
    void cifraContrasena() {
        when(passwordEncoder.encode("nueva")).thenReturn("cifrada");

        service.updateUsuario(ID, new UsuarioUpdateRequest(), "nueva");

        assertEquals("cifrada", usuario.getPassword());
    }

    @Test
    @DisplayName("conserva la contraseña si llega nula o vacía")
    void conservaContrasena() {
        service.updateUsuario(ID, new UsuarioUpdateRequest(), null);
        service.updateUsuario(ID, new UsuarioUpdateRequest(), "");

        assertEquals("anterior", usuario.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("actualiza la sede si existe")
    void actualizaSede() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setSedeNombre("Lima Norte");
        when(sedeRepository.findByNombre("Lima Norte")).thenReturn(Optional.of(new Sede(2L, "Lima Norte")));

        assertEquals("Lima Norte", service.updateUsuario(ID, request, null).getSedeNombre());
    }

    @Test
    @DisplayName("conserva la sede si la indicada no existe")
    void sedeInexistente() {
        usuario.setSede(new Sede(1L, "Lima Centro"));
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setSedeNombre("No existe");
        when(sedeRepository.findByNombre("No existe")).thenReturn(Optional.empty());

        assertEquals("Lima Centro", service.updateUsuario(ID, request, null).getSedeNombre());
    }

    @Test
    @DisplayName("reemplaza las zonas de un agente de seguridad e ignora las inexistentes")
    void zonasDeSeguridad() {
        usuario.getRoles().clear();
        usuario.getRoles().add(new Rol(3L, ERol.ROLE_SEGURIDAD));
        usuario.getZonas().add(zona("Anterior"));
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setZonasNombres(List.of("Patio", "No existe"));
        when(zonaRepository.findByNombre("Patio")).thenReturn(Optional.of(zona("Patio")));
        when(zonaRepository.findByNombre("No existe")).thenReturn(Optional.empty());

        UsuarioDTO dto = service.updateUsuario(ID, request, null);

        assertThat(dto.getZonasNombres()).containsExactly("Patio");
    }

    @Test
    @DisplayName("no modifica las zonas de un usuario sin rol de seguridad")
    void zonasDeOtroRol() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setZonasNombres(List.of("Patio"));

        UsuarioDTO dto = service.updateUsuario(ID, request, null);

        assertThat(dto.getZonasNombres()).isEmpty();
        verify(zonaRepository, never()).findByNombre(anyString());
    }

    @Test
    @DisplayName("B03: rechaza un correo que pertenece a otro usuario, sin cambiar nada")
    void correoRepetido() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombreCompleto("Otro nombre");
        request.setCorreo("otro@utp.pe");
        when(usuarioRepository.existsByCorreoAndIdNot("otro@utp.pe", ID)).thenReturn(true);

        BadRequestException error = assertThrows(BadRequestException.class, () -> service.updateUsuario(ID, request, null));

        assertThat(error.getMessage()).contains("correo");
        assertEquals("Ana Ruiz", usuario.getNombreCompleto());
        assertEquals("ana@utp.pe", usuario.getCorreo());
    }

    @Test
    @DisplayName("B03: acepta un correo nuevo y disponible")
    void correoDisponible() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setCorreo("nuevo@utp.pe");
        when(usuarioRepository.existsByCorreoAndIdNot("nuevo@utp.pe", ID)).thenReturn(false);

        assertEquals("nuevo@utp.pe", service.updateUsuario(ID, request, null).getCorreo());
    }

    @Test
    @DisplayName("B03: no valida la unicidad si el correo no cambia")
    void correoSinCambios() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setCorreo("ana@utp.pe");

        service.updateUsuario(ID, request, null);

        verify(usuarioRepository, never()).existsByCorreoAndIdNot(anyString(), any());
    }

    @Test
    @DisplayName("un teléfono repetido no deja cambios parciales")
    void telefonoRepetidoSinCambiosParciales() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setNombreCompleto("Otro nombre");
        request.setTelefono("888");
        when(usuarioRepository.existsByTelefonoAndIdNot("888", ID)).thenReturn(true);

        assertNull(service.updateUsuario(ID, request, null));

        assertEquals("Ana Ruiz", usuario.getNombreCompleto());
        assertEquals("999", usuario.getTelefono());
    }
}
