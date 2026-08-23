package com.utp_reporta_backend.modules.auth.service;

import com.utp_reporta_backend.modules.auth.dto.request.LoginDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroAdminDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroSeguridadDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroUsuarioDTO;
import com.utp_reporta_backend.modules.auth.dto.response.JwtAuthResponseDTO;

public interface AuthService {
	 JwtAuthResponseDTO login(LoginDTO loginDTO);
	 String registrarUsuario(RegistroUsuarioDTO registroUsuarioDTO);
	 String registrarAdmin(RegistroAdminDTO registroAdminDTO);
	 String registrarSeguridad(RegistroSeguridadDTO registroSeguridadDTO);

}


