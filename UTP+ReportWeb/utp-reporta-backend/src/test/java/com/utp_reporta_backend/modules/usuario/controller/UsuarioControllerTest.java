package com.utp_reporta_backend.modules.usuario.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.utp_reporta_backend.common.exception.GlobalExceptionHandler;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.usuario.service.UsuarioService;
import com.utp_reporta_backend.modules.usuario.service.command.impl.UsuarioCommandServiceImpl;
import com.utp_reporta_backend.modules.usuario.service.impl.UsuarioServiceImpl;
import com.utp_reporta_backend.modules.usuario.service.query.UsuarioQueryService;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock private UsuarioService usuarioService;
    @InjectMocks private UsuarioController controller;

    private final UsuarioDTO dto = new UsuarioDTO();
    private final List<UsuarioDTO> lista = List.of(dto);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private void verificarOk(ResponseEntity<?> respuesta, Object cuerpo) {
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(cuerpo, respuesta.getBody());
    }

    @Test
    @DisplayName("DELETE /{id} elimina y responde 204 (F10)")
    void eliminaUsuario() {
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteUsuario(1L).getStatusCode());
        verify(usuarioService).deleteUsuario(1L);
    }

    @Test
    @DisplayName("PUT /{id} responde 200 con el usuario actualizado")
    void actualizaUsuario() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        request.setPassword("clave");
        when(usuarioService.updateUsuario(1L, request, "clave")).thenReturn(dto);

        verificarOk(controller.updateUsuario(1L, request), dto);
    }

    @Test
    @DisplayName("PUT /{id} responde 409 si el servicio rechaza el cambio")
    void actualizaConConflicto() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        when(usuarioService.updateUsuario(1L, request, null)).thenReturn(null);

        assertEquals(HttpStatus.CONFLICT, controller.updateUsuario(1L, request).getStatusCode());
    }

    @Test
    @DisplayName("B05: PUT /{id} responde 404 con el motivo si el usuario no existe")
    void actualizaUsuarioInexistente() throws Exception {
        UsuarioRepository repositorio = mock(UsuarioRepository.class);
        when(repositorio.findById(99L)).thenReturn(Optional.empty());
        UsuarioCommandServiceImpl comandos = new UsuarioCommandServiceImpl(repositorio,
                mock(SedeRepository.class), mock(ZonaRepository.class), mock(PasswordEncoder.class));
        UsuarioController conServicioReal = new UsuarioController(
                new UsuarioServiceImpl(mock(UsuarioQueryService.class), comandos));

        MockMvcBuilders.standaloneSetup(conServicioReal).setControllerAdvice(new GlobalExceptionHandler()).build()
                .perform(put("/api/usuarios/99").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Usuario no encontrado con ID: 99"));
    }

    @Test
    @DisplayName("GET /me responde 401 sin autenticación")
    void perfilSinAutenticacion() {
        assertEquals(HttpStatus.UNAUTHORIZED, controller.miPerfil().getStatusCode());
    }

    @Test
    @DisplayName("GET /me devuelve el perfil del usuario autenticado")
    void perfilAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ana", "x"));
        when(usuarioService.getCurrentProfile("ana")).thenReturn(dto);

        verificarOk(controller.miPerfil(), dto);
    }

    @Test
    @DisplayName("GET /me responde 404 si el usuario autenticado ya no existe")
    void perfilInexistente() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("ana", "x"));
        when(usuarioService.getCurrentProfile("ana")).thenReturn(null);

        assertEquals(HttpStatus.NOT_FOUND, controller.miPerfil().getStatusCode());
    }

    @Test
    @DisplayName("GET /codigo/{codigo} responde 200 o 404")
    void porCodigo() {
        when(usuarioService.findByCodigo("ana")).thenReturn(dto);
        when(usuarioService.findByCodigo("nadie")).thenReturn(null);

        verificarOk(controller.getUsuarioByCodigo("ana"), dto);
        assertEquals(HttpStatus.NOT_FOUND, controller.getUsuarioByCodigo("nadie").getStatusCode());
    }

    @Test
    @DisplayName("PUT /{id}/enabled responde 200 o 404")
    void cambiaEstado() {
        when(usuarioService.updateUsuarioEnabledStatus(1L, false)).thenReturn(dto);
        when(usuarioService.updateUsuarioEnabledStatus(99L, false)).thenReturn(null);

        verificarOk(controller.updateUsuarioEnabledStatus(1L, false), dto);
        assertEquals(HttpStatus.NOT_FOUND, controller.updateUsuarioEnabledStatus(99L, false).getStatusCode());
    }

    @Test
    @DisplayName("los listados responden 200 con los datos del servicio")
    void listados() {
        when(usuarioService.getAllUsuarios()).thenReturn(lista);
        when(usuarioService.getUsuariosByRolUsuario()).thenReturn(lista);
        when(usuarioService.getUsuariosByRolAdmin()).thenReturn(lista);
        when(usuarioService.getUsuariosByRolSeguridad()).thenReturn(lista);
        when(usuarioService.getUsuariosByTipoDocente()).thenReturn(lista);
        when(usuarioService.getUsuariosByTipoAlumno()).thenReturn(lista);
        when(usuarioService.getUsuariosByEnabledStatus(true)).thenReturn(lista);
        when(usuarioService.getAllUsersExcludingSuperAdmin()).thenReturn(lista);
        when(usuarioService.getFilteredSeguridadUsers(5L, 1L)).thenReturn(lista);

        verificarOk(controller.getAllUsuarios(), lista);
        verificarOk(controller.getUsuariosByRolUsuario(), lista);
        verificarOk(controller.getUsuariosByRolAdmin(), lista);
        verificarOk(controller.getUsuariosByRolSeguridad(), lista);
        verificarOk(controller.getUsuariosByTipoDocente(), lista);
        verificarOk(controller.getUsuariosByTipoAlumno(), lista);
        verificarOk(controller.getUsuariosByEnabledStatus(true), lista);
        verificarOk(controller.getAllUsersExcludingSuperAdmin(), lista);
        verificarOk(controller.getFilteredSeguridadUsers(5L, 1L), lista);
    }

    @Test
    @DisplayName("GET /check-telefono devuelve el resultado del servicio")
    void revisaTelefono() {
        when(usuarioService.isTelefonoUnique("999", 1L)).thenReturn(false);

        assertEquals(Boolean.FALSE, controller.checkTelefonoUnique("999", 1L).getBody());
    }
}
