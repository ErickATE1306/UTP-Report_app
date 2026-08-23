package com.utp_reporta_backend.modules.usuario.service.query;

import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import java.util.List;

public interface UsuarioQueryService {
    List<UsuarioDTO> getAllUsuarios();
    List<UsuarioDTO> getUsuariosByRolUsuario();
    List<UsuarioDTO> getUsuariosByRolAdmin();
    List<UsuarioDTO> getUsuariosByRolSeguridad();
    List<UsuarioDTO> getFilteredSeguridadUsers(Long zonaId, Long sedeId);
    List<UsuarioDTO> getUsuariosByTipoDocente();
    List<UsuarioDTO> getUsuariosByTipoAlumno();
    UsuarioDTO findByCodigo(String codigo);
    List<UsuarioDTO> getUsuariosByEnabledStatus(boolean enabled);
    List<UsuarioDTO> getAllUsersExcludingSuperAdmin();
    Boolean isTelefonoUnique(String telefono, Long id);
    UsuarioDTO getCurrentProfile(String principal);
}
