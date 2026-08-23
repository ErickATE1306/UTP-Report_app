package com.utp_reporta_backend.modules.usuario.service;

import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import java.util.List;

public interface UsuarioService {
    List<UsuarioDTO> getAllUsuarios();
    List<UsuarioDTO> getUsuariosByRolUsuario();
    List<UsuarioDTO> getUsuariosByRolAdmin();
    List<UsuarioDTO> getUsuariosByRolSeguridad();
    List<UsuarioDTO> getFilteredSeguridadUsers(Long zonaId, Long sedeId);
    List<UsuarioDTO> getUsuariosByTipoDocente();
    List<UsuarioDTO> getUsuariosByTipoAlumno();
    UsuarioDTO findByCodigo(String codigo);
    List<UsuarioDTO> getUsuariosByEnabledStatus(boolean enabled);
    UsuarioDTO updateUsuarioEnabledStatus(Long id, boolean enabled);
    UsuarioDTO updateUsuario(Long id, UsuarioUpdateRequest request, String password);
    List<UsuarioDTO> getAllUsersExcludingSuperAdmin();
    Boolean isTelefonoUnique(String telefono, Long id); // New method for phone number uniqueness check
    UsuarioDTO getCurrentProfile(String principal);
}


