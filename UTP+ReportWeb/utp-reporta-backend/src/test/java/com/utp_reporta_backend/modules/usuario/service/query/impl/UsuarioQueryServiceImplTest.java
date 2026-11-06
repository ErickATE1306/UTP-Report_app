package com.utp_reporta_backend.modules.usuario.service.query.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.mapper.UsuarioMapper;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.model.enums.TipoUsuario;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.model.Zona;

@ExtendWith(MockitoExtension.class)
class UsuarioQueryServiceImplTest {

    private static final LocalDate FECHA = LocalDate.of(2025, 10, 11);

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @InjectMocks private UsuarioQueryServiceImpl service;

    private Usuario usuario(String username, ERol rol, TipoUsuario tipo) {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername(username);
        usuario.setNombreCompleto("Nombre " + username);
        usuario.setCorreo(username + "@utp.pe");
        usuario.setTelefono("999");
        usuario.setTipoUsuario(tipo);
        usuario.setSede(new Sede(1L, "Lima Centro"));
        Zona zona = new Zona();
        zona.setNombre("Patio");
        usuario.setZonas(Set.of(zona));
        usuario.setIntentosReporte(2);
        usuario.setFechaUltimoReporte(FECHA);
        usuario.setEnabled(true);
        usuario.setRoles(Set.of(new Rol(1L, rol)));
        return usuario;
    }

    private void verificarMapeoCompleto(UsuarioDTO dto) {
        assertEquals(1L, dto.getId());
        assertEquals("Nombre ana", dto.getNombreCompleto());
        assertEquals("ana", dto.getUsername());
        assertEquals("ana@utp.pe", dto.getCorreo());
        assertEquals("999", dto.getTelefono());
        assertEquals("Lima Centro", dto.getSedeNombre());
        assertThat(dto.getZonasNombres()).containsExactly("Patio");
        assertEquals(2, dto.getIntentos());
        assertEquals(FECHA, dto.getFechaUltimoReporte());
        assertTrue(dto.isEnabled());
    }

    private List<String> usernames(List<UsuarioDTO> usuarios) {
        return usuarios.stream().map(UsuarioDTO::getUsername).toList();
    }

    private void conTodosLosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(
                usuario("alumno", ERol.ROLE_USUARIO, TipoUsuario.ALUMNO),
                usuario("docente", ERol.ROLE_USUARIO, TipoUsuario.DOCENTE),
                usuario("admin", ERol.ROLE_ADMIN, null),
                usuario("seguridad", ERol.ROLE_SEGURIDAD, null)));
    }

    @Test
    @DisplayName("getAllUsuarios copia todos los campos al DTO")
    void todosLosUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario("ana", ERol.ROLE_ADMIN, TipoUsuario.DOCENTE)));

        List<UsuarioDTO> resultado = service.getAllUsuarios();

        assertEquals(1, resultado.size());
        verificarMapeoCompleto(resultado.get(0));
        assertEquals(TipoUsuario.DOCENTE, resultado.get(0).getTipoUsuario());
        assertThat(resultado.get(0).getRoles()).containsExactly("ROLE_ADMIN");
    }

    @Test
    @DisplayName("deja la sede en null si el usuario no tiene una")
    void usuarioSinSede() {
        Usuario usuario = usuario("ana", ERol.ROLE_USUARIO, null);
        usuario.setSede(null);
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        assertNull(service.getAllUsuarios().get(0).getSedeNombre());
    }

    @Test
    @DisplayName("getUsuariosByRolUsuario filtra por ROLE_USUARIO")
    void porRolUsuario() {
        conTodosLosUsuarios();
        assertThat(usernames(service.getUsuariosByRolUsuario())).containsExactly("alumno", "docente");
    }

    @Test
    @DisplayName("getUsuariosByRolAdmin filtra por ROLE_ADMIN")
    void porRolAdmin() {
        conTodosLosUsuarios();
        assertThat(usernames(service.getUsuariosByRolAdmin())).containsExactly("admin");
    }

    @Test
    @DisplayName("getUsuariosByRolSeguridad filtra por ROLE_SEGURIDAD")
    void porRolSeguridad() {
        conTodosLosUsuarios();
        assertThat(usernames(service.getUsuariosByRolSeguridad())).containsExactly("seguridad");
    }

    @Test
    @DisplayName("getUsuariosByTipoDocente filtra por tipo e ignora usuarios sin tipo")
    void porTipoDocente() {
        conTodosLosUsuarios();
        assertThat(usernames(service.getUsuariosByTipoDocente())).containsExactly("docente");
    }

    @Test
    @DisplayName("getUsuariosByTipoAlumno filtra por tipo e ignora usuarios sin tipo")
    void porTipoAlumno() {
        conTodosLosUsuarios();
        assertThat(usernames(service.getUsuariosByTipoAlumno())).containsExactly("alumno");
    }

    @Test
    @DisplayName("B04: getUsuariosByTipoAlumno incluye la fecha del último reporte")
    void alumnoConFechaUltimoReporte() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario("ana", ERol.ROLE_USUARIO, TipoUsuario.ALUMNO)));

        verificarMapeoCompleto(service.getUsuariosByTipoAlumno().get(0));
    }

    @Test
    @DisplayName("getFilteredSeguridadUsers filtra por zona y sede")
    void seguridadPorZonaYSede() {
        when(usuarioRepository.findByRoles_NombreAndZonas_IdAndSede_Id(ERol.ROLE_SEGURIDAD, 5L, 1L))
                .thenReturn(List.of(usuario("ana", ERol.ROLE_SEGURIDAD, null)));

        verificarMapeoCompleto(service.getFilteredSeguridadUsers(5L, 1L).get(0));
    }

    @Test
    @DisplayName("getFilteredSeguridadUsers filtra solo por zona")
    void seguridadPorZona() {
        when(usuarioRepository.findByRoles_NombreAndZonas_Id(ERol.ROLE_SEGURIDAD, 5L))
                .thenReturn(List.of(usuario("ana", ERol.ROLE_SEGURIDAD, null)));

        assertThat(usernames(service.getFilteredSeguridadUsers(5L, null))).containsExactly("ana");
    }

    @Test
    @DisplayName("getFilteredSeguridadUsers filtra solo por sede")
    void seguridadPorSede() {
        when(usuarioRepository.findByRoles_NombreAndSede_Id(ERol.ROLE_SEGURIDAD, 1L))
                .thenReturn(List.of(usuario("ana", ERol.ROLE_SEGURIDAD, null)));

        assertThat(usernames(service.getFilteredSeguridadUsers(null, 1L))).containsExactly("ana");
    }

    @Test
    @DisplayName("getFilteredSeguridadUsers sin filtros lista a todo el personal de seguridad")
    void seguridadSinFiltros() {
        when(usuarioRepository.findByRoles_Nombre(ERol.ROLE_SEGURIDAD))
                .thenReturn(List.of(usuario("ana", ERol.ROLE_SEGURIDAD, null)));

        assertThat(usernames(service.getFilteredSeguridadUsers(null, null))).containsExactly("ana");
    }

    @Test
    @DisplayName("findByCodigo busca por username")
    void porCodigo() {
        when(usuarioRepository.findByUsername("ana")).thenReturn(Optional.of(usuario("ana", ERol.ROLE_USUARIO, null)));

        verificarMapeoCompleto(service.findByCodigo("ana"));
    }

    @Test
    @DisplayName("findByCodigo devuelve null si no existe")
    void porCodigoInexistente() {
        when(usuarioRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertNull(service.findByCodigo("nadie"));
    }

    @Test
    @DisplayName("getUsuariosByEnabledStatus consulta por el estado indicado")
    void porEstado() {
        when(usuarioRepository.findByEnabled(true)).thenReturn(List.of(usuario("ana", ERol.ROLE_USUARIO, null)));

        verificarMapeoCompleto(service.getUsuariosByEnabledStatus(true).get(0));
    }

    @Test
    @DisplayName("getAllUsersExcludingSuperAdmin excluye ROLE_SUPERADMIN")
    void sinSuperAdmin() {
        when(usuarioRepository.findByRoles_NombreNot(ERol.ROLE_SUPERADMIN))
                .thenReturn(List.of(usuario("ana", ERol.ROLE_ADMIN, null)));

        verificarMapeoCompleto(service.getAllUsersExcludingSuperAdmin().get(0));
    }

    @Test
    @DisplayName("isTelefonoUnique al crear revisa a todos los usuarios")
    void telefonoAlCrear() {
        when(usuarioRepository.existsByTelefono("999")).thenReturn(true);
        when(usuarioRepository.existsByTelefono("888")).thenReturn(false);

        assertFalse(service.isTelefonoUnique("999", null));
        assertTrue(service.isTelefonoUnique("888", null));
        verify(usuarioRepository, never()).existsByTelefonoAndIdNot("999", null);
    }

    @Test
    @DisplayName("isTelefonoUnique al actualizar excluye al propio usuario")
    void telefonoAlActualizar() {
        when(usuarioRepository.existsByTelefonoAndIdNot("999", 1L)).thenReturn(true);
        when(usuarioRepository.existsByTelefonoAndIdNot("888", 1L)).thenReturn(false);

        assertFalse(service.isTelefonoUnique("999", 1L));
        assertTrue(service.isTelefonoUnique("888", 1L));
    }

    @Test
    @DisplayName("getCurrentProfile busca por username o correo y usa el mapper")
    void perfilActual() {
        Usuario usuario = usuario("ana", ERol.ROLE_USUARIO, null);
        UsuarioDTO dto = new UsuarioDTO();
        when(usuarioRepository.findByUsernameOrCorreo("ana", "ana")).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(dto);

        assertSame(dto, service.getCurrentProfile("ana"));
    }

    @Test
    @DisplayName("getCurrentProfile devuelve null si no existe")
    void perfilInexistente() {
        when(usuarioRepository.findByUsernameOrCorreo("nadie", "nadie")).thenReturn(Optional.empty());

        assertNull(service.getCurrentProfile("nadie"));
    }
}
