package com.utp_reporta_backend.modules.auth.service.impl;

import com.utp_reporta_backend.modules.auth.dto.request.LoginDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroAdminDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroSeguridadDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroUsuarioDTO;
import com.utp_reporta_backend.modules.auth.dto.response.JwtAuthResponseDTO;
import com.utp_reporta_backend.modules.auth.service.AuthService;
import com.utp_reporta_backend.modules.auth.service.command.AuthCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthCommandService commandService;

    public JwtAuthResponseDTO login(LoginDTO request) {
        String token = commandService.login(request);
        JwtAuthResponseDTO response = new JwtAuthResponseDTO();
        response.setToken(token);
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null) {
            response.setRoles(authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
        }
        return response;
    }

    public String registrarUsuario(RegistroUsuarioDTO request){return commandService.registrarUsuario(request);}
    public String registrarAdmin(RegistroAdminDTO request){return commandService.registrarAdmin(request);}
    public String registrarSeguridad(RegistroSeguridadDTO request){return commandService.registrarSeguridad(request);}
}
