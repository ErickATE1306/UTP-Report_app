package com.utp_reporta_backend.common.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI utpReportaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("UTP+ Reporta API")
                        .description("API REST del monolito modular UTP+ Reporta")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi allApi() {
        return group("00-all", "/api/**");
    }

    @Bean
    public GroupedOpenApi authApi() {
        return group("01-auth", "/api/auth/**");
    }

    @Bean
    public GroupedOpenApi usuariosApi() {
        return group("02-usuarios", "/api/usuarios/**");
    }

    @Bean
    public GroupedOpenApi catalogosApi() {
        return group("03-catalogos", "/api/sedes/**", "/api/zonas/**", "/api/tipoincidentes/**");
    }

    @Bean
    public GroupedOpenApi reportesApi() {
        return group("04-reportes", "/api/reportes/**");
    }

    private GroupedOpenApi group(String name, String... paths) {
        return GroupedOpenApi.builder().group(name).pathsToMatch(paths).build();
    }
}
