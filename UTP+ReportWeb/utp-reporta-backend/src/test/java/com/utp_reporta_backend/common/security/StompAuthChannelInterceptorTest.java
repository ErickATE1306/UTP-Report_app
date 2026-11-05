package com.utp_reporta_backend.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.utp_reporta_backend.modules.auth.service.CustomUserDetailsService;

@ExtendWith(MockitoExtension.class)
class StompAuthChannelInterceptorTest {
    private static final String TOKEN = "token-valido";

    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private MessageChannel canal;
    @InjectMocks private StompAuthChannelInterceptor interceptor;

    @Test
    void conectaConUnTokenValidoYAsociaAlUsuario() {
        tokenDe("ana", true);
        StompHeaderAccessor accessor = conectar("Bearer " + TOKEN);

        interceptor.preSend(mensaje(accessor), canal);

        assertThat(accessor.getUser()).isNotNull();
        assertThat(accessor.getUser().getName()).isEqualTo("ana");
    }

    @Test
    void rechazaConectarSinToken() {
        assertRechazaConexion(conectar(null));
    }

    @Test
    void rechazaUnEncabezadoQueNoEsBearer() {
        assertRechazaConexion(conectar("Basic abc"));
    }

    @Test
    void rechazaUnTokenInvalido() {
        when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(false);

        assertRechazaConexion(conectar("Bearer " + TOKEN));
    }

    @Test
    void rechazaUnaCuentaDeshabilitada() {
        tokenDe("ana", false);

        assertRechazaConexion(conectar("Bearer " + TOKEN));
    }

    @Test
    void rechazaUnaCuentaEliminada() {
        when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJWT(TOKEN)).thenReturn("ana");
        when(userDetailsService.loadUserByUsername("ana")).thenThrow(new UsernameNotFoundException("ana"));

        assertThatThrownBy(() -> interceptor.preSend(mensaje(conectar("Bearer " + TOKEN)), canal))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void permiteSuscribirseALosCanalesPropios() {
        for (String destino : new String[] { "/topic/zone-status", "/user/queue/notifications", "/topic/report-status.ana" }) {
            Message<?> mensaje = mensaje(suscribir("ana", destino));

            assertThat(interceptor.preSend(mensaje, canal)).isSameAs(mensaje);
        }
    }

    @Test
    void noPermiteEscucharLosReportesDeOtroUsuario() {
        assertRechazaSuscripcion("ana", "/topic/report-status.beto");
    }

    @Test
    void noPermiteUnNombreQueSoloEmpiezaIgual() {
        assertRechazaSuscripcion("ana", "/topic/report-status.anabel");
    }

    @Test
    void noPermiteComodinesNiLaColaInternaDeOtraSesion() {
        assertRechazaSuscripcion("ana", "/topic/report-status.*");
        assertRechazaSuscripcion("ana", "/queue/notifications-user123");
        assertRechazaSuscripcion("ana", "/user/beto/queue/notifications");
    }

    @Test
    void noPermiteSuscribirseSinSesion() {
        assertRechazaSuscripcion(null, "/topic/zone-status");
    }

    @Test
    void noPermitePublicarMensajes() {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SEND);
        accessor.setDestination("/topic/zone-status");
        accessor.setUser(new TestingAuthenticationToken("ana", null));

        assertThatThrownBy(() -> interceptor.preSend(mensaje(accessor), canal))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void dejaPasarLosDemasComandos() {
        Message<?> mensaje = mensaje(StompHeaderAccessor.create(StompCommand.DISCONNECT));

        assertThat(interceptor.preSend(mensaje, canal)).isSameAs(mensaje);
    }

    private void tokenDe(String username, boolean habilitada) {
        when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromJWT(TOKEN)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username))
                .thenReturn(User.withUsername(username).password("x").disabled(!habilitada).roles("USUARIO").build());
    }

    private StompHeaderAccessor conectar(String autorizacion) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        if (autorizacion != null) {
            accessor.setNativeHeader("Authorization", autorizacion);
        }
        return accessor;
    }

    private StompHeaderAccessor suscribir(String usuario, String destino) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destino);
        if (usuario != null) {
            accessor.setUser(new TestingAuthenticationToken(usuario, null));
        }
        return accessor;
    }

    private Message<?> mensaje(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private void assertRechazaConexion(StompHeaderAccessor accessor) {
        assertThatThrownBy(() -> interceptor.preSend(mensaje(accessor), canal))
                .isInstanceOf(BadCredentialsException.class);
    }

    private void assertRechazaSuscripcion(String usuario, String destino) {
        assertThatThrownBy(() -> interceptor.preSend(mensaje(suscribir(usuario, destino)), canal))
                .isInstanceOf(AccessDeniedException.class);
    }
}
