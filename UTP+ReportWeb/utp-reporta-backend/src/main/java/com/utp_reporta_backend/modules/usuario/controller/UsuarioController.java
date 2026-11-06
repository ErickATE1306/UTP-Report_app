package com.utp_reporta_backend.modules.usuario.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.dto.request.UsuarioUpdateRequest;
import com.utp_reporta_backend.modules.usuario.service.UsuarioService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarioService;

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioDTO> updateUsuario(
            @PathVariable Long id,
            @RequestBody UsuarioUpdateRequest request) { // La contraseña viaja en el cuerpo, nunca en la URL
        UsuarioDTO updatedUsuario = usuarioService.updateUsuario(id, request, request.getPassword());
        if (updatedUsuario != null) {
            return ResponseEntity.ok(updatedUsuario);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null); // 409: teléfono repetido
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUsuario(@PathVariable Long id) {
        usuarioService.deleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seguridad/filter")
    public ResponseEntity<List<UsuarioDTO>> getFilteredSeguridadUsers(
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long sedeId) {
        List<UsuarioDTO> usuarios = usuarioService.getFilteredSeguridadUsers(zonaId, sedeId);
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioDTO> miPerfil(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        UsuarioDTO dto = usuarioService.getCurrentProfile(auth.getName());
        if (dto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> getAllUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.getAllUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/rol/usuario")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByRolUsuario() {
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByRolUsuario();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/rol/admin")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByRolAdmin() {
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByRolAdmin();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/rol/seguridad")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByRolSeguridad() {
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByRolSeguridad();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/tipo/docente")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByTipoDocente() {
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByTipoDocente();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/tipo/alumno")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByTipoAlumno() {
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByTipoAlumno();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<UsuarioDTO> getUsuarioByCodigo(@PathVariable String codigo) {
        UsuarioDTO usuario = usuarioService.findByCodigo(codigo);
        if (usuario != null) {
            return ResponseEntity.ok(usuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/enabled")
    public ResponseEntity<List<UsuarioDTO>> getUsuariosByEnabledStatus(@RequestParam boolean enabled) {
        List<UsuarioDTO> usuarios = usuarioService.getUsuariosByEnabledStatus(enabled);
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}/enabled")
    public ResponseEntity<UsuarioDTO> updateUsuarioEnabledStatus(@PathVariable Long id, @RequestParam boolean enabled) {
        UsuarioDTO updatedUsuario = usuarioService.updateUsuarioEnabledStatus(id, enabled);
        if (updatedUsuario != null) {
            return ResponseEntity.ok(updatedUsuario);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/excluding-superadmin")
    public ResponseEntity<List<UsuarioDTO>> getAllUsersExcludingSuperAdmin() {
        List<UsuarioDTO> usuarios = usuarioService.getAllUsersExcludingSuperAdmin();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/check-telefono")
    public ResponseEntity<Boolean> checkTelefonoUnique(@RequestParam String telefono, @RequestParam(required = false) Long id) {
        return ResponseEntity.ok(usuarioService.isTelefonoUnique(telefono, id));
    }
}


