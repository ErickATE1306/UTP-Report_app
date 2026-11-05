package com.utp_reporta_backend.common.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

class JwtTokenProviderTest {

    // Clave solo para pruebas: generada para este archivo, sin uso real.
    private static final String SECRETO = "dGVzdC1zb2xvLXBydWViYXMtMzItYnl0ZXMtbWluaW1vIQ==";

    private final JwtTokenProvider provider = new JwtTokenProvider(SECRETO);
    private final UserDetails usuario = new User("ana", "x", List.of());

    private Key claveDelProveedor() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRETO));
    }

    private Claims leer(String token) {
        return Jwts.parserBuilder().setSigningKey(claveDelProveedor()).build().parseClaimsJws(token).getBody();
    }

    @Test
    @DisplayName("genera un token válido que contiene el username")
    void generaToken() {
        String token = provider.generateToken(usuario);

        assertTrue(provider.validateToken(token));
        assertEquals("ana", provider.getUsernameFromJWT(token));
    }

    @Test
    @DisplayName("el token expira a las 10 horas")
    void expiraEnDiezHoras() {
        Claims claims = leer(provider.generateToken(usuario));

        long duracion = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
        assertEquals(10 * 60 * 60 * 1000L, duracion);
    }

    @Test
    @DisplayName("incluye los reclamos adicionales")
    void reclamosAdicionales() {
        Claims claims = leer(provider.generateToken(Map.of("rol", "ROLE_ADMIN"), usuario));

        assertEquals("ROLE_ADMIN", claims.get("rol"));
        assertEquals("ana", claims.getSubject());
    }

    @Test
    @DisplayName("rechaza un token expirado")
    void tokenExpirado() {
        String token = Jwts.builder()
                .setSubject("ana")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(claveDelProveedor(), SignatureAlgorithm.HS256)
                .compact();

        assertFalse(provider.validateToken(token));
    }

    @Test
    @DisplayName("rechaza un token firmado con otra clave")
    void otraClave() {
        String token = Jwts.builder()
                .setSubject("ana")
                .setExpiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.secretKeyFor(SignatureAlgorithm.HS256))
                .compact();

        assertFalse(provider.validateToken(token));
    }

    @Test
    @DisplayName("rechaza un token alterado")
    void tokenAlterado() {
        String token = provider.generateToken(usuario);
        String[] partes = token.split("\\.");
        String firmaAlterada = (partes[2].startsWith("A") ? "B" : "A") + partes[2].substring(1);

        assertFalse(provider.validateToken(partes[0] + "." + partes[1] + "." + firmaAlterada));
    }

    @Test
    @DisplayName("rechaza un texto que no es un token")
    void noEsToken() {
        assertFalse(provider.validateToken("no-es-un-token"));
        assertFalse(provider.validateToken(""));
    }

    @Test
    @DisplayName("getUsernameFromJWT lanza un error con un token inválido")
    void usernameDeTokenInvalido() {
        assertThrows(JwtException.class, () -> provider.getUsernameFromJWT("no-es-un-token"));
    }
}
