package com.utp_reporta_backend.modules.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.AuthorityUtils;

import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.usuario.dto.response.UsuarioDTO;
import com.utp_reporta_backend.modules.usuario.mapper.UsuarioMapper;
import com.utp_reporta_backend.modules.usuario.mapper.UsuarioMapperImpl;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.model.enums.TipoUsuario;
import com.utp_reporta_backend.modules.zona.model.Zona;

class UsuarioModeloYMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapperImpl();

    private Zona zona(String nombre) {
        Zona zona = new Zona();
        zona.setNombre(nombre);
        return zona;
    }

    @Test
    @DisplayName("el builder aplica los valores por defecto")
    void valoresPorDefecto() {
        Usuario usuario = Usuario.builder().username("ana").build();

        assertTrue(usuario.isEnabled());
        assertEquals(0, usuario.getFailedLoginAttempts());
        assertThat(usuario.getRoles()).isEmpty();
        assertThat(usuario.getZonas()).isEmpty();
    }

    @Test
    @DisplayName("getAuthorities convierte los roles en autoridades")
    void autoridades() {
        Usuario usuario = Usuario.builder()
                .roles(Set.of(new Rol(1L, ERol.ROLE_ADMIN), new Rol(2L, ERol.ROLE_USUARIO)))
                .build();

        assertThat(AuthorityUtils.authorityListToSet(usuario.getAuthorities()))
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USUARIO");
    }

    @Test
    @DisplayName("isEnabled refleja el estado del usuario y los demás indicadores son verdaderos")
    void indicadoresDeCuenta() {
        Usuario usuario = Usuario.builder().enabled(false).build();

        assertFalse(usuario.isEnabled());
        assertTrue(usuario.isAccountNonExpired());
        assertTrue(usuario.isAccountNonLocked());
        assertTrue(usuario.isCredentialsNonExpired());
    }

    @Test
    @DisplayName("el mapper convierte todos los campos")
    void mapeoCompleto() {
        LocalDate fecha = LocalDate.of(2025, 10, 11);
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("ana")
                .nombreCompleto("Ana Ruiz")
                .correo("ana@utp.pe")
                .telefono("999")
                .tipoUsuario(TipoUsuario.ALUMNO)
                .sede(new Sede(1L, "Lima Centro"))
                .zonas(Set.of(zona("Patio")))
                .intentosReporte(2)
                .fechaUltimoReporte(fecha)
                .roles(Set.of(new Rol(1L, ERol.ROLE_USUARIO)))
                .build();

        UsuarioDTO dto = mapper.toResponse(usuario);

        assertEquals(1L, dto.getId());
        assertEquals("ana", dto.getUsername());
        assertEquals("Ana Ruiz", dto.getNombreCompleto());
        assertEquals("ana@utp.pe", dto.getCorreo());
        assertEquals("999", dto.getTelefono());
        assertEquals(TipoUsuario.ALUMNO, dto.getTipoUsuario());
        assertEquals("Lima Centro", dto.getSedeNombre());
        assertThat(dto.getZonasNombres()).containsExactly("Patio");
        assertEquals(2, dto.getIntentos());
        assertEquals(fecha, dto.getFechaUltimoReporte());
        assertTrue(dto.isEnabled());
        assertThat(dto.getRoles()).containsExactly("ROLE_USUARIO");
    }

    @Test
    @DisplayName("el mapper tolera un usuario sin sede, zonas ni roles")
    void mapeoMinimo() {
        UsuarioDTO dto = mapper.toResponse(Usuario.builder().username("ana").build());

        assertNull(dto.getSedeNombre());
        assertThat(dto.getZonasNombres()).isEmpty();
        assertThat(dto.getRoles()).isEmpty();
    }

    @Test
    @DisplayName("el mapper devuelve null si el usuario es null")
    void mapeoNulo() {
        assertNull(mapper.toResponse(null));
    }
}
