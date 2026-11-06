package com.utp_reporta_backend.modules.auth.service.command.impl;

import com.utp_reporta_backend.common.exception.BadRequestException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.transaction.support.SimpleTransactionStatus;

import com.utp_reporta_backend.common.security.JwtTokenProvider;
import com.utp_reporta_backend.modules.auth.dto.request.LoginDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroAdminDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroSeguridadDTO;
import com.utp_reporta_backend.modules.auth.dto.request.RegistroUsuarioDTO;
import com.utp_reporta_backend.modules.auth.service.command.AuthCommandService;
import com.utp_reporta_backend.modules.sede.model.Sede;
import com.utp_reporta_backend.modules.sede.repository.SedeRepository;
import com.utp_reporta_backend.modules.time.service.TimeService;
import com.utp_reporta_backend.modules.usuario.model.Rol;
import com.utp_reporta_backend.modules.usuario.model.Usuario;
import com.utp_reporta_backend.modules.usuario.model.enums.ERol;
import com.utp_reporta_backend.modules.usuario.model.enums.TipoUsuario;
import com.utp_reporta_backend.modules.usuario.repository.RolRepository;
import com.utp_reporta_backend.modules.usuario.repository.UsuarioRepository;
import com.utp_reporta_backend.modules.zona.model.Zona;
import com.utp_reporta_backend.modules.zona.repository.ZonaRepository;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceImplTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2025, 10, 11, 10, 0);

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RolRepository rolRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private ZonaRepository zonaRepository;
    @Mock private SedeRepository sedeRepository;
    @Mock private TimeService timeService;

    @InjectMocks private AuthCommandServiceImpl service;

    private final Sede sede = new Sede(1L, "Lima Centro");

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private Usuario guardado() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        return captor.getValue();
    }

    @Nested
    @DisplayName("login")
    class Login {
        private LoginDTO credenciales() {
            LoginDTO dto = new LoginDTO();
            dto.setUsernameOrCorreo("ana");
            dto.setPassword("secreto");
            return dto;
        }

        private Usuario usuarioExistente(int intentos, LocalDateTime bloqueo) {
            Usuario usuario = new Usuario();
            usuario.setUsername("ana");
            usuario.setFailedLoginAttempts(intentos);
            usuario.setLockoutTime(bloqueo);
            when(usuarioRepository.findByUsernameOrCorreo("ana", "ana")).thenReturn(Optional.of(usuario));
            return usuario;
        }

        @Test
        @DisplayName("con credenciales válidas devuelve el token y reinicia los intentos")
        void credencialesValidas() {
            Usuario usuario = usuarioExistente(2, null);
            UserDetails principal = mock(UserDetails.class);
            Authentication autenticacion = mock(Authentication.class);
            when(autenticacion.getPrincipal()).thenReturn(principal);
            when(authenticationManager.authenticate(any())).thenReturn(autenticacion);
            when(jwtTokenProvider.generateToken(principal)).thenReturn("jwt");

            assertEquals("jwt", service.login(credenciales()));

            assertEquals(0, usuario.getFailedLoginAttempts());
            assertThat(usuario.getLockoutTime()).isNull();
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(autenticacion);
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("rechaza a un usuario inexistente sin intentar autenticar")
        void usuarioInexistente() {
            when(usuarioRepository.findByUsernameOrCorreo("ana", "ana")).thenReturn(Optional.empty());

            AuthenticationException error = assertThrows(AuthenticationException.class, () -> service.login(credenciales()));

            assertEquals("Credenciales inválidas.", error.getMessage());
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("una contraseña incorrecta suma un intento sin bloquear")
        void contrasenaIncorrecta() {
            Usuario usuario = usuarioExistente(0, null);
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("x"));

            AuthenticationException error = assertThrows(AuthenticationException.class, () -> service.login(credenciales()));

            assertEquals("Credenciales inválidas.", error.getMessage());
            assertEquals(1, usuario.getFailedLoginAttempts());
            assertThat(usuario.getLockoutTime()).isNull();
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("el tercer intento fallido bloquea la cuenta 15 minutos")
        void tercerIntentoBloquea() {
            Usuario usuario = usuarioExistente(2, null);
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("x"));
            when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);

            assertThrows(AuthenticationException.class, () -> service.login(credenciales()));

            assertEquals(3, usuario.getFailedLoginAttempts());
            assertEquals(AHORA.plusMinutes(15), usuario.getLockoutTime());
        }

        @Test
        @DisplayName("una contraseña incorrecta confirma la transacción para conservar el intento (B08)")
        void falloConfirmaLaTransaccion() {
            usuarioExistente(0, null);
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("x"));
            PlatformTransactionManager transacciones = mock(PlatformTransactionManager.class);
            when(transacciones.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
            ProxyFactory fabrica = new ProxyFactory(service);
            fabrica.addAdvice(new TransactionInterceptor(transacciones, new AnnotationTransactionAttributeSource()));
            AuthCommandService conTransaccion = (AuthCommandService) fabrica.getProxy();

            assertThrows(AuthenticationException.class, () -> conTransaccion.login(credenciales()));

            verify(transacciones).commit(any());
            verify(transacciones, never()).rollback(any());
        }

        @Test
        @DisplayName("una cuenta bloqueada se rechaza sin validar la contraseña")
        void cuentaBloqueada() {
            usuarioExistente(3, AHORA.plusMinutes(10));
            when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);

            AuthenticationException error = assertThrows(AuthenticationException.class, () -> service.login(credenciales()));

            assertThat(error.getMessage()).contains("bloqueada").contains("10 minutos");
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("al vencer el bloqueo reinicia los intentos y permite autenticar")
        void bloqueoVencido() {
            Usuario usuario = usuarioExistente(3, AHORA.minusMinutes(1));
            when(timeService.getCurrentLocalDateTimePeru()).thenReturn(AHORA);
            when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("x"));

            assertThrows(AuthenticationException.class, () -> service.login(credenciales()));

            assertEquals(1, usuario.getFailedLoginAttempts());
            assertThat(usuario.getLockoutTime()).isNull();
            verify(authenticationManager).authenticate(any());
        }
    }

    @Nested
    @DisplayName("registrarUsuario")
    class RegistrarUsuario {
        private RegistroUsuarioDTO dto() {
            RegistroUsuarioDTO dto = new RegistroUsuarioDTO();
            dto.setNombreCompleto("Ana Ruiz");
            dto.setUsername("ana");
            dto.setCorreo("ana@utp.pe");
            dto.setPassword("secreto");
            dto.setTelefono("999");
            dto.setTipoUsuario(TipoUsuario.ALUMNO);
            dto.setSedeId(1L);
            return dto;
        }

        @Test
        @DisplayName("rechaza un username repetido")
        void usernameRepetido() {
            when(usuarioRepository.existsByUsername("ana")).thenReturn(true);

            assertEquals("El nombre de usuario ya existe", assertThrows(BadRequestException.class, () -> service.registrarUsuario(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("rechaza un correo repetido")
        void correoRepetido() {
            when(usuarioRepository.existsByUsername("ana")).thenReturn(false);
            when(usuarioRepository.existsByCorreo("ana@utp.pe")).thenReturn(true);

            assertEquals("El email ya está en uso", assertThrows(BadRequestException.class, () -> service.registrarUsuario(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("falla si la sede no existe")
        void sedeInexistente() {
            when(sedeRepository.findById(1L)).thenReturn(Optional.empty());

            RuntimeException error = assertThrows(RuntimeException.class, () -> service.registrarUsuario(dto()));

            assertEquals("Sede no encontrada", error.getMessage());
        }

        @Test
        @DisplayName("guarda al usuario con la contraseña cifrada y ROLE_USUARIO")
        void registroExitoso() {
            Rol rol = new Rol(1L, ERol.ROLE_USUARIO);
            when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
            when(rolRepository.findByNombre(ERol.ROLE_USUARIO)).thenReturn(Optional.of(rol));
            when(passwordEncoder.encode("secreto")).thenReturn("cifrada");

            assertEquals("Usuario registrado exitosamente", service.registrarUsuario(dto()));

            Usuario usuario = guardado();
            assertEquals("cifrada", usuario.getPassword());
            assertEquals(TipoUsuario.ALUMNO, usuario.getTipoUsuario());
            assertEquals(sede, usuario.getSede());
            assertThat(usuario.getRoles()).containsExactly(rol);
        }
    }

    @Nested
    @DisplayName("registrarAdmin")
    class RegistrarAdmin {
        private RegistroAdminDTO dto() {
            RegistroAdminDTO dto = new RegistroAdminDTO();
            dto.setUsername("admin");
            dto.setCorreo("admin@utp.pe");
            dto.setPassword("secreto");
            dto.setSedeId(1L);
            return dto;
        }

        @Test
        @DisplayName("rechaza un username repetido")
        void usernameRepetido() {
            when(usuarioRepository.existsByUsername("admin")).thenReturn(true);
            assertEquals("El nombre de usuario ya existe", assertThrows(BadRequestException.class, () -> service.registrarAdmin(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("rechaza un correo repetido")
        void correoRepetido() {
            when(usuarioRepository.existsByCorreo("admin@utp.pe")).thenReturn(true);
            assertEquals("El email ya está en uso", assertThrows(BadRequestException.class, () -> service.registrarAdmin(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("asigna los roles de administrador y usuario")
        void registroExitoso() {
            Rol admin = new Rol(1L, ERol.ROLE_ADMIN);
            Rol usuario = new Rol(2L, ERol.ROLE_USUARIO);
            when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
            when(rolRepository.findByNombre(ERol.ROLE_ADMIN)).thenReturn(Optional.of(admin));
            when(rolRepository.findByNombre(ERol.ROLE_USUARIO)).thenReturn(Optional.of(usuario));
            when(passwordEncoder.encode("secreto")).thenReturn("cifrada");

            assertEquals("Admin registrado exitosamente", service.registrarAdmin(dto()));

            assertThat(guardado().getRoles()).containsExactlyInAnyOrder(admin, usuario);
        }
    }

    @Nested
    @DisplayName("registrarSeguridad")
    class RegistrarSeguridad {
        private RegistroSeguridadDTO dto() {
            RegistroSeguridadDTO dto = new RegistroSeguridadDTO();
            dto.setUsername("seg");
            dto.setCorreo("seg@utp.pe");
            dto.setPassword("secreto");
            dto.setSedeId(1L);
            dto.setZonaIds(Set.of(5L));
            return dto;
        }

        @Test
        @DisplayName("exige la sede")
        void sinSede() {
            RegistroSeguridadDTO dto = dto();
            dto.setSedeId(null);
            assertEquals("El ID de la sede (sedeId) no puede ser nulo.", assertThrows(BadRequestException.class, () -> service.registrarSeguridad(dto)).getMessage());
        }

        @Test
        @DisplayName("exige la lista de zonas")
        void sinZonas() {
            RegistroSeguridadDTO dto = dto();
            dto.setZonaIds(null);
            assertEquals("La lista de IDs de zona (zonaIds) no puede ser nula.", assertThrows(BadRequestException.class, () -> service.registrarSeguridad(dto)).getMessage());
        }

        @Test
        @DisplayName("rechaza un correo repetido")
        void correoRepetido() {
            when(usuarioRepository.existsByCorreo("seg@utp.pe")).thenReturn(true);
            assertEquals("El correo ya está en uso", assertThrows(BadRequestException.class, () -> service.registrarSeguridad(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("rechaza un username repetido")
        void usernameRepetido() {
            when(usuarioRepository.existsByUsername("seg")).thenReturn(true);
            assertEquals("El nombre de usuario ya existe", assertThrows(BadRequestException.class, () -> service.registrarSeguridad(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("exige al menos una zona existente")
        void zonasInexistentes() {
            when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
            when(zonaRepository.findAllById(Set.of(5L))).thenReturn(List.of());

            assertEquals("Debe seleccionar al menos una zona válida", assertThrows(BadRequestException.class, () -> service.registrarSeguridad(dto())).getMessage());
            verify(usuarioRepository, never()).save(any());
        }

        @Test
        @DisplayName("guarda al personal con sus zonas y ROLE_SEGURIDAD")
        void registroExitoso() {
            Zona zona = new Zona();
            zona.setId(5L);
            Rol rol = new Rol(3L, ERol.ROLE_SEGURIDAD);
            when(sedeRepository.findById(1L)).thenReturn(Optional.of(sede));
            when(zonaRepository.findAllById(Set.of(5L))).thenReturn(List.of(zona));
            when(rolRepository.findByNombre(ERol.ROLE_SEGURIDAD)).thenReturn(Optional.of(rol));
            when(passwordEncoder.encode("secreto")).thenReturn("cifrada");

            assertEquals("Usuario de seguridad registrado exitosamente", service.registrarSeguridad(dto()));

            Usuario usuario = guardado();
            assertThat(usuario.getZonas()).containsExactly(zona);
            assertThat(usuario.getRoles()).containsExactly(rol);
            assertThat(usuario.isEnabled()).isTrue();
            assertEquals(0, usuario.getIntentosReporte());
        }
    }
}
