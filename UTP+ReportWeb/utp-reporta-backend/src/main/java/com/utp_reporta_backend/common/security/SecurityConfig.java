package com.utp_reporta_backend.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
//Configuración de seguridad con JWT. 
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	// Configura la cadena de filtros de seguridad
	@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Permite a Spring mostrar el error real en lugar de convertirlo en 401.
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // Swagger solo existe con SWAGGER_ENABLED=true; /ws valida el JWT en StompAuthChannelInterceptor.
                .requestMatchers("/swagger-ui/**","/swagger-ui-custom.html","/v3/api-docs/**","/index.html/**", "/ws/**").permitAll()
                // El alta de cuentas solo se ofrece en las pantallas de SuperAdmin.
                .requestMatchers("/api/auth/**").hasRole("SUPERADMIN")
                // Usuarios
                .requestMatchers(HttpMethod.GET, "/api/usuarios/me").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole("SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("SUPERADMIN")
                .requestMatchers("/api/usuarios/**").hasAnyRole("ADMIN", "SUPERADMIN")
                // Catálogos: cualquier sesión los consulta; solo la administración los cambia.
                .requestMatchers(HttpMethod.GET, "/api/sedes/**", "/api/zonas/**", "/api/tipoincidentes/**").authenticated()
                .requestMatchers("/api/zonas/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("/api/sedes/**", "/api/tipoincidentes/**").hasRole("SUPERADMIN")
                // Reportes: las rutas de un solo dueño las completa ReporteAccessPolicy.
                .requestMatchers(HttpMethod.POST, "/api/reportes/gestion/*").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("/api/reportes/gestion/**").authenticated() // @PreAuthorize en el controlador
                .requestMatchers(HttpMethod.GET, "/api/reportes/export/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers(HttpMethod.GET, "/api/reportes", "/api/reportes/filter", "/api/reportes/filtered").hasAnyRole("ADMIN", "SUPERADMIN", "SEGURIDAD")
                .requestMatchers(HttpMethod.PUT, "/api/reportes/*").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/reportes/*").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("/api/reportes/**").authenticated()
                .requestMatchers("/api/v1/SUPERADMIN/**").hasAnyRole("SUPERADMIN")
                .requestMatchers("/api/v1/ADMIN/**").hasAnyRole("ADMIN")
                .requestMatchers("/api/v1/USUARIO/**").hasAnyRole("USUARIO")
                .requestMatchers("/api/v1/SEGURIDAD/**").hasAnyRole("SEGURIDAD")
                .anyRequest().authenticated()
            );
        // Agrega el filtro de autenticación JWT antes del filtro de nombre de usuario y contraseña
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
	
	// Configura el administrador de autenticación
	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) 
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    // Configura el codificador de contraseñas
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
		
	}

}

