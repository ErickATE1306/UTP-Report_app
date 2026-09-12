package com.utp_reporta_backend.modules.usuario.service.command;

import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;

public interface UsuarioCommandService {
    UsuarioDTO updateUsuarioEnabledStatus(Long id, boolean enabled);
    UsuarioDTO updateUsuario(Long id, UsuarioUpdateRequest request, String password);
}
