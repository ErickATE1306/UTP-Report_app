package com.utp_reporta_backend.modules.usuario.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.service.command.UsuarioCommandService;
import com.utp_reporta_backend.modules.usuario.service.query.UsuarioQueryService;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock private UsuarioQueryService queryService;
    @Mock private UsuarioCommandService commandService;
    @InjectMocks private UsuarioServiceImpl service;

    private final UsuarioDTO dto = new UsuarioDTO();
    private final List<UsuarioDTO> lista = List.of(dto);

    @Test
    @DisplayName("delega los listados en el servicio de consulta")
    void delegaListados() {
        when(queryService.getAllUsuarios()).thenReturn(lista);
        when(queryService.getUsuariosByRolUsuario()).thenReturn(lista);
        when(queryService.getUsuariosByRolAdmin()).thenReturn(lista);
        when(queryService.getUsuariosByRolSeguridad()).thenReturn(lista);
        when(queryService.getFilteredSeguridadUsers(5L, 1L)).thenReturn(lista);
        when(queryService.getUsuariosByTipoDocente()).thenReturn(lista);
        when(queryService.getUsuariosByTipoAlumno()).thenReturn(lista);
        when(queryService.getUsuariosByEnabledStatus(true)).thenReturn(lista);
        when(queryService.getAllUsersExcludingSuperAdmin()).thenReturn(lista);

        assertSame(lista, service.getAllUsuarios());
        assertSame(lista, service.getUsuariosByRolUsuario());
        assertSame(lista, service.getUsuariosByRolAdmin());
        assertSame(lista, service.getUsuariosByRolSeguridad());
        assertSame(lista, service.getFilteredSeguridadUsers(5L, 1L));
        assertSame(lista, service.getUsuariosByTipoDocente());
        assertSame(lista, service.getUsuariosByTipoAlumno());
        assertSame(lista, service.getUsuariosByEnabledStatus(true));
        assertSame(lista, service.getAllUsersExcludingSuperAdmin());
    }

    @Test
    @DisplayName("delega las búsquedas individuales en el servicio de consulta")
    void delegaBusquedas() {
        when(queryService.findByCodigo("ana")).thenReturn(dto);
        when(queryService.getCurrentProfile("ana")).thenReturn(dto);
        when(queryService.isTelefonoUnique("999", 1L)).thenReturn(true);

        assertSame(dto, service.findByCodigo("ana"));
        assertSame(dto, service.getCurrentProfile("ana"));
        assertEquals(true, service.isTelefonoUnique("999", 1L));
    }

    @Test
    @DisplayName("delega las actualizaciones en el servicio de comandos")
    void delegaActualizaciones() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        when(commandService.updateUsuarioEnabledStatus(1L, false)).thenReturn(dto);
        when(commandService.updateUsuario(1L, request, "clave")).thenReturn(dto);

        assertSame(dto, service.updateUsuarioEnabledStatus(1L, false));
        assertSame(dto, service.updateUsuario(1L, request, "clave"));
    }
}
