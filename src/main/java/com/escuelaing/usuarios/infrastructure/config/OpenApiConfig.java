package com.escuelaing.usuarios.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI usuariosServiceOpenAPI() {
        final String jwtScheme = "bearerAuth";
        final String apiKeyScheme = "internalApiKey";

        return new OpenAPI()
                .info(new Info()
                        .title("usuarios-service API")
                        .description("Gestión de usuarios, perfiles, álbum de fotos e intereses - Plataforma PATRICIA")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(jwtScheme))
                .addSecurityItem(new SecurityRequirement().addList(apiKeyScheme))
                .components(new Components()
                        .addSecuritySchemes(jwtScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT"))
                        .addSecuritySchemes(apiKeyScheme, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Internal-Api-Key")));
    }
}
