package com.utp_reporta_backend.modules.reporte.controller;

import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

// Reglas de acceso que dependen del dueño del reporte; las de rol están en SecurityConfig.
@Component
@RequiredArgsConstructor
public class ReporteAccessPolicy {
    private static final Set<String> ROLES_GESTION = Set.of("ROLE_ADMIN", "ROLE_SUPERADMIN", "ROLE_SEGURIDAD");

    private final UsuarioRepository usuarioRepository;

    public void verificarLecturaPorUsername(Authentication auth, String username) {
        if (!auth.getName().equals(username) && !gestionaReportes(auth)) {
            throw new AccessDeniedException("No puedes consultar los reportes de otro usuario");
        }
    }

    public void verificarLectura(Authentication auth, Long usuarioIdDelReporte) {
        if (!gestionaReportes(auth) && !esElUsuarioAutenticado(auth, usuarioIdDelReporte)) {
            throw new AccessDeniedException("No puedes consultar este reporte");
        }
    }

    // Un reporte siempre se crea a nombre de quien inició sesión, sea cual sea su rol.
    public void verificarAutoria(Authentication auth, Long usuarioId) {
        if (!esElUsuarioAutenticado(auth, usuarioId)) {
            throw new AccessDeniedException("No puedes crear reportes a nombre de otro usuario");
        }
    }

    private boolean gestionaReportes(Authentication auth) {
        return auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(ROLES_GESTION::contains);
    }

    private boolean esElUsuarioAutenticado(Authentication auth, Long usuarioId) {
        return usuarioRepository.findByUsername(auth.getName())
                .map(usuario -> Objects.equals(usuario.getId(), usuarioId))
                .orElse(false);
    }
}
