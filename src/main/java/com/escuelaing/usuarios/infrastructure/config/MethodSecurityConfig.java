package com.escuelaing.usuarios.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Habilita el soporte de @PreAuthorize / @PostAuthorize en los controllers,
 * usado para restringir endpoints como PUT /{id}/roles a ADMIN/MODERATOR.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
