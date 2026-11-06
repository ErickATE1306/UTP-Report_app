package com.utp_reporta_backend.common.security;

import java.security.Principal;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.utp_reporta_backend.modules.auth.service.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

// SockJS no permite enviar encabezados HTTP, por eso /ws es público y el JWT se valida en el CONNECT de STOMP.
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    private static final String CANAL_ZONAS = "/topic/zone-status";
    private static final String COLA_PERSONAL = "/user/queue/notifications";
    private static final String PREFIJO_REPORTES = "/topic/report-status.";
    private static final String BEARER = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        StompCommand comando = accessor.getCommand();
        if (comando == StompCommand.CONNECT || comando == StompCommand.STOMP) {
            accessor.setUser(autenticar(accessor));
        } else if (comando == StompCommand.SUBSCRIBE) {
            verificarSuscripcion(accessor.getUser(), accessor.getDestination());
        } else if (comando == StompCommand.SEND) {
            // Solo el servidor publica: un cliente podría falsificar avisos de zona o de reportes.
            throw new AccessDeniedException("Los clientes no pueden publicar mensajes");
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken autenticar(StompHeaderAccessor accessor) {
        String encabezado = accessor.getFirstNativeHeader("Authorization");
        if (encabezado == null || !encabezado.startsWith(BEARER)) {
            throw new BadCredentialsException("Falta el token de acceso");
        }
        String token = encabezado.substring(BEARER.length());
        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Token de acceso no válido");
        }
        UserDetails cuenta = userDetailsService.loadUserByUsername(jwtTokenProvider.getUsernameFromJWT(token));
        if (!cuenta.isEnabled()) {
            throw new BadCredentialsException("La cuenta está deshabilitada");
        }
        return new UsernamePasswordAuthenticationToken(cuenta, null, cuenta.getAuthorities());
    }

    private void verificarSuscripcion(Principal usuario, String destino) {
        if (usuario == null) {
            throw new AccessDeniedException("Se requiere iniciar sesión");
        }
        boolean permitido = CANAL_ZONAS.equals(destino)
                || COLA_PERSONAL.equals(destino)
                || (PREFIJO_REPORTES + usuario.getName()).equals(destino);
        if (!permitido) {
            throw new AccessDeniedException("No puedes suscribirte a " + destino);
        }
    }
}
