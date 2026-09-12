package com.utp_reporta_backend.modules.auth.service.command;

import com.utp_reporta_backend.modules.auth.dto.request.LoginDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroAdminDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroSeguridadDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroUsuarioDTO;

public interface AuthCommandService {
    String login(LoginDTO request);
    String registrarUsuario(RegistroUsuarioDTO request);
    String registrarAdmin(RegistroAdminDTO request);
    String registrarSeguridad(RegistroSeguridadDTO request);
}
