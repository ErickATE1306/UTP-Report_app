package com.utp_reporta_backend.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.utp_reporta_backend.modules.auth.controller.AuthController;
import com.utp_reporta_backend.modules.auth.service.AuthService;
import com.utp_reporta_backend.modules.auth.service.CustomUserDetailsService;
import com.utp_reporta_backend.modules.reporte.controller.ReporteAccessPolicy;
import com.utp_reporta_backend.modules.reporte.controller.ReporteController;
import com.utp_reporta_backend.modules.reporte.controller.ReporteGestionController;
import com.utp_reporta_backend.modules.reporte.dto.response.ReporteDTO;
import com.utp_reporta_backend.modules.reporte.service.ReporteGestionService;
import com.utp_reporta_backend.modules.reporte.service.ReporteService;
import com.utp_reporta_backend.modules.sede.controller.SedeController;
import com.utp_reporta_backend.modules.sede.service.SedeService;
import com.utp_reporta_backend.modules.tipoincidente.controller.TipoIncidenteController;
import com.utp_reporta_backend.modules.tipoincidente.service.TipoIncidenteService;
import com.utp_reporta_backend.modules.usuario.controller.UsuarioController;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.usuario.service.UsuarioService;
import com.utp_reporta_backend.modules.zona.controller.ZonaController;
import com.utp_reporta_backend.modules.zona.service.ZonaService;

import jakarta.servlet.ServletException;

/** Comprueba quién puede llamar a cada endpoint, con la cadena real de Spring Security y JWT real. */
@WebMvcTest(controllers = {
        AuthController.class, UsuarioController.class, ReporteController.class, ReporteGestionController.class,
        SedeController.class, ZonaController.class, TipoIncidenteController.class })
@Import({ SecurityConfig.class, JwtAuthenticationEntryPoint.class, JwtTokenProvider.class, ReporteAccessPolicy.class })
@TestPropertySource(properties = "app.jwt.secret=dGVzdC1zb2xvLXBydWViYXMtMzItYnl0ZXMtbWluaW1vIQ==")
class ApiAuthorizationTest {
    private static final String USUARIO = "USUARIO";
    private static final String SEGURIDAD = "SEGURIDAD";
    private static final String ADMIN = "ADMIN";
    private static final String SUPERADMIN = "SUPERADMIN";
    private static final List<String> ROLES = List.of(USUARIO, SEGURIDAD, ADMIN, SUPERADMIN);

    private static final Set<String> CUALQUIER_SESION = Set.copyOf(ROLES);
    private static final Set<String> ADMINISTRACION = Set.of(ADMIN, SUPERADMIN);
    private static final Set<String> GESTION = Set.of(SEGURIDAD, ADMIN, SUPERADMIN);
    private static final Set<String> SOLO_SUPERADMIN = Set.of(SUPERADMIN);

    private static final Long ID_DUENO = 7L;

    @Autowired private MockMvc mvc;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    @MockitoBean private AuthService authService;
    @MockitoBean private CustomUserDetailsService userDetailsService;
    @MockitoBean private UsuarioService usuarioService;
    @MockitoBean private UsuarioRepository usuarioRepository;
    @MockitoBean private ReporteService reporteService;
    @MockitoBean private ReporteGestionService reporteGestionService;
    @MockitoBean private SedeService sedeService;
    @MockitoBean private ZonaService zonaService;
    @MockitoBean private TipoIncidenteService tipoIncidenteService;

    private static Stream<Arguments> matriz() {
        return Stream.of(
                // Altas de cuentas
                ruta("POST", "/api/auth/registrarUsuario", SOLO_SUPERADMIN),
                ruta("POST", "/api/auth/registrarAdmin", SOLO_SUPERADMIN),
                ruta("POST", "/api/auth/registrarSeguridad", SOLO_SUPERADMIN),
                // Usuarios
                ruta("GET", "/api/usuarios/me", CUALQUIER_SESION),
                ruta("GET", "/api/usuarios", ADMINISTRACION),
                ruta("GET", "/api/usuarios/seguridad/filter", ADMINISTRACION),
                ruta("GET", "/api/usuarios/rol/seguridad", ADMINISTRACION),
                ruta("GET", "/api/usuarios/codigo/U1", ADMINISTRACION),
                ruta("GET", "/api/usuarios/enabled", ADMINISTRACION),
                ruta("GET", "/api/usuarios/excluding-superadmin", ADMINISTRACION),
                ruta("GET", "/api/usuarios/check-telefono?telefono=1", ADMINISTRACION),
                ruta("PUT", "/api/usuarios/1", SOLO_SUPERADMIN),
                ruta("DELETE", "/api/usuarios/1", SOLO_SUPERADMIN),
                ruta("PUT", "/api/usuarios/1/enabled?enabled=false", SOLO_SUPERADMIN),
                // Catálogos
                ruta("GET", "/api/sedes", CUALQUIER_SESION),
                ruta("GET", "/api/zonas", CUALQUIER_SESION),
                ruta("GET", "/api/zonas/sede/1", CUALQUIER_SESION),
                ruta("GET", "/api/tipoincidentes", CUALQUIER_SESION),
                ruta("POST", "/api/sedes", SOLO_SUPERADMIN),
                ruta("PUT", "/api/sedes/1", SOLO_SUPERADMIN),
                ruta("DELETE", "/api/sedes/1", SOLO_SUPERADMIN),
                ruta("POST", "/api/tipoincidentes", SOLO_SUPERADMIN),
                ruta("PUT", "/api/tipoincidentes/1", SOLO_SUPERADMIN),
                ruta("DELETE", "/api/tipoincidentes/1", SOLO_SUPERADMIN),
                ruta("POST", "/api/zonas", ADMINISTRACION),
                ruta("PUT", "/api/zonas/1", ADMINISTRACION),
                ruta("PATCH", "/api/zonas/1/activo", ADMINISTRACION),
                ruta("DELETE", "/api/zonas/1", ADMINISTRACION),
                // Reportes
                ruta("GET", "/api/reportes", GESTION),
                ruta("GET", "/api/reportes/filter", GESTION),
                ruta("GET", "/api/reportes/filtered", GESTION),
                ruta("GET", "/api/reportes/export/excel", ADMINISTRACION),
                ruta("PUT", "/api/reportes/1", ADMINISTRACION),
                ruta("DELETE", "/api/reportes/1", ADMINISTRACION),
                // Gestión de reportes (URL + @PreAuthorize del controlador)
                ruta("POST", "/api/reportes/gestion/1", ADMINISTRACION),
                ruta("PUT", "/api/reportes/gestion/1/ir-a-zona", Set.of(SEGURIDAD)),
                ruta("PUT", "/api/reportes/gestion/1/zona-ubicada", Set.of(SEGURIDAD)),
                ruta("PUT", "/api/reportes/gestion/1/completar?mensajeSeguridad=listo", Set.of(SEGURIDAD)),
                ruta("PUT", "/api/reportes/gestion/1/marcar-resuelto-admin", Set.of(ADMIN)),
                ruta("PUT", "/api/reportes/gestion/1/rechazar-admin", Set.of(ADMIN)));
    }

    private static Arguments ruta(String metodo, String url, Set<String> permitidos) {
        return Arguments.of(metodo, url, permitidos);
    }

    @ParameterizedTest(name = "{0} {1} sin sesión responde 401")
    @MethodSource("matriz")
    void sinSesionResponde401(String metodo, String url, Set<String> permitidos) throws Exception {
        assertThat(estado(peticion(metodo, url))).isEqualTo(401);
    }

    @ParameterizedTest(name = "{0} {1} solo admite {2}")
    @MethodSource("matriz")
    void cadaRolRecibeElPermisoEsperado(String metodo, String url, Set<String> permitidos) throws Exception {
        for (String rol : ROLES) {
            int estado = estado(peticion(metodo, url).with(user("alguien").roles(rol)));
            if (permitidos.contains(rol)) {
                assertThat(estado).as("%s debe poder pasar la seguridad", rol).isNotIn(401, 403);
            } else {
                assertThat(estado).as("%s debe ser rechazado", rol).isEqualTo(403);
            }
        }
    }

    @Test
    void elLoginEsPublico() throws Exception {
        int estado = estado(request(HttpMethod.POST, "/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON).content("{}"));

        assertThat(estado).isNotIn(401, 403);
    }

    @Test
    void elPreflightDeCorsNoExigeSesion() throws Exception {
        mvc.perform(options("/api/reportes")
                .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk());
    }

    @Test
    void unUsuarioConsultaSuPropioReporte() throws Exception {
        reporteDeDueno();
        sesionDe("ana", ID_DUENO);

        mvc.perform(get("/api/reportes/1").with(user("ana").roles(USUARIO))).andExpect(status().isOk());
    }

    @Test
    void unUsuarioNoConsultaElReporteDeOtro() throws Exception {
        reporteDeDueno();
        sesionDe("beto", 8L);

        mvc.perform(get("/api/reportes/1").with(user("beto").roles(USUARIO))).andExpect(status().isForbidden());
    }

    @Test
    void unUsuarioNoDescubreSiExisteUnReporteAjeno() throws Exception {
        sesionDe("beto", 8L);

        mvc.perform(get("/api/reportes/99").with(user("beto").roles(USUARIO))).andExpect(status().isForbidden());
    }

    @Test
    void seguridadConsultaCualquierReporte() throws Exception {
        reporteDeDueno();

        mvc.perform(get("/api/reportes/1").with(user("guardia").roles(SEGURIDAD))).andExpect(status().isOk());
    }

    @Test
    void unUsuarioListaSusReportesPorUsername() throws Exception {
        mvc.perform(get("/api/reportes/by-username/ana").with(user("ana").roles(USUARIO))).andExpect(status().isOk());
    }

    @Test
    void unUsuarioNoListaLosReportesDeOtro() throws Exception {
        mvc.perform(get("/api/reportes/by-username/ana").with(user("beto").roles(USUARIO)))
                .andExpect(status().isForbidden());
        verify(reporteService, never()).getReportsByUsername(any());
    }

    @Test
    void administracionListaReportesDeCualquierUsuario() throws Exception {
        mvc.perform(get("/api/reportes/by-username/ana").with(user("jefa").roles(ADMIN))).andExpect(status().isOk());
    }

    @Test
    void unReporteSeCreaANombreDeQuienIniciaSesion() throws Exception {
        sesionDe("ana", ID_DUENO);

        mvc.perform(nuevoReporte(ID_DUENO).with(user("ana").roles(USUARIO))).andExpect(status().isCreated());
    }

    @Test
    void nadieCreaReportesANombreDeOtro() throws Exception {
        sesionDe("beto", 8L);

        mvc.perform(nuevoReporte(ID_DUENO).with(user("beto").roles(USUARIO))).andExpect(status().isForbidden());
        verify(reporteService, never()).createReporte(any());
    }

    @Test
    void crearReporteSinSesionResponde401() throws Exception {
        mvc.perform(nuevoReporte(ID_DUENO)).andExpect(status().isUnauthorized());
    }

    @Test
    void laContrasenaSeLeeDelCuerpoYNoDeLaUrl() throws Exception {
        mvc.perform(put("/api/usuarios/1?password=en-la-url").with(user("root").roles(SUPERADMIN))
                .contentType(MediaType.APPLICATION_JSON).content("{\"password\":\"en-el-cuerpo\"}"));

        verify(usuarioService).updateUsuario(any(), any(), eq("en-el-cuerpo"));
    }

    @Test
    void unTokenValidoAutenticaConLosRolesDeLaBaseDeDatos() throws Exception {
        UserDetails cuenta = cuenta("root", true, SUPERADMIN);
        when(userDetailsService.loadUserByUsername("root")).thenReturn(cuenta);

        assertThat(estado(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(cuenta)))).isEqualTo(200);
    }

    @Test
    void unTokenValidoNoDaMasPermisosQueSuRol() throws Exception {
        UserDetails cuenta = cuenta("ana", true, USUARIO);
        when(userDetailsService.loadUserByUsername("ana")).thenReturn(cuenta);

        assertThat(estado(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, bearer(cuenta)))).isEqualTo(403);
    }

    @Test
    void unaCuentaDeshabilitadaPierdeElAccesoConSuTokenVigente() throws Exception {
        UserDetails cuenta = cuenta("ana", false, USUARIO);
        when(userDetailsService.loadUserByUsername("ana")).thenReturn(cuenta);

        assertThat(estado(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(cuenta)))).isEqualTo(401);
    }

    @Test
    void elTokenDeUnaCuentaEliminadaResponde401() throws Exception {
        UserDetails cuenta = cuenta("ana", true, USUARIO);
        when(userDetailsService.loadUserByUsername("ana")).thenThrow(new UsernameNotFoundException("ana"));

        assertThat(estado(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, bearer(cuenta)))).isEqualTo(401);
    }

    @Test
    void unTokenAlteradoResponde401() throws Exception {
        String alterado = bearer(cuenta("ana", true, SUPERADMIN)) + "x";

        assertThat(estado(get("/api/usuarios").header(HttpHeaders.AUTHORIZATION, alterado))).isEqualTo(401);
    }

    private MockHttpServletRequestBuilder peticion(String metodo, String url) {
        return request(HttpMethod.valueOf(metodo), url)
                .contentType(MediaType.APPLICATION_JSON).content("{}");
    }

    /** Una excepción del servicio simulado significa que la petición ya superó la seguridad. */
    private int estado(MockHttpServletRequestBuilder peticion) throws Exception {
        try {
            return mvc.perform(peticion).andReturn().getResponse().getStatus();
        } catch (ServletException e) {
            return 500;
        }
    }

    private MockHttpServletRequestBuilder nuevoReporte(Long usuarioId) {
        return multipart("/api/reportes")
                .param("tipoIncidenteId", "1")
                .param("zonaId", "1")
                .param("descripcion", "Fuga de agua")
                .param("isAnonimo", "false")
                .param("usuarioId", usuarioId.toString());
    }

    private void reporteDeDueno() {
        ReporteDTO reporte = new ReporteDTO();
        reporte.setUsuarioId(ID_DUENO);
        when(reporteService.getReporteById(1L)).thenReturn(reporte);
    }

    private void sesionDe(String username, Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));
    }

    private UserDetails cuenta(String username, boolean habilitada, String rol) {
        return User.withUsername(username).password("x").disabled(!habilitada).roles(rol).build();
    }

    private String bearer(UserDetails cuenta) {
        return "Bearer " + jwtTokenProvider.generateToken(cuenta);
    }
}
