package com.utp_reporta_backend.modules.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp_reporta_backend.modules.auth.dto.request.LoginDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroAdminDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroSeguridadDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroUsuarioDTO;
import com.utp_reporta_backend.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;

//Controlador para manejar la autenticación y registro de usuarios.
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//Controlador para manejar la autenticación y registro de usuarios.
public class AuthController {
    private final AuthService authService;
    // Endpoint para el login de usuarios
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authService.login(loginDTO));
    }

    //Registar usuario con con roles alumno y docente
    @PostMapping("/registrarUsuario")
    public ResponseEntity<String> registrarUsuario(@RequestBody RegistroUsuarioDTO registroDTO) {
        String respuesta = authService.registrarUsuario(registroDTO);
        return ResponseEntity.ok(respuesta);
    }
    //Registar usuario con rol admin
    @PostMapping("/registrarAdmin")
    public ResponseEntity<String> registrarAdmin(@RequestBody RegistroAdminDTO registroAdminDTO) {
        String respuesta = authService.registrarAdmin(registroAdminDTO);
        return ResponseEntity.ok(respuesta);
    }
    //Registar usuario con rol seguridad
    @PostMapping("/registrarSeguridad")
    public ResponseEntity<String> registrarSeguridad(@RequestBody RegistroSeguridadDTO dto) {
        String resultado = authService.registrarSeguridad(dto);
        return ResponseEntity.ok(resultado);
    }

    
    

}


