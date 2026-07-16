package com.escuelaing.usuarios.infrastructure.config;

import com.escuelaing.usuarios.infrastructure.security.InternalApiKeyFilter;
import com.escuelaing.usuarios.infrastructure.security.JwtAuthenticationFilter;
import com.escuelaing.usuarios.infrastructure.security.JwtTokenParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de seguridad de usuarios-service.
 *
 * Dos cadenas de filtros independientes:
 * - /internal/**  -> InternalApiKeyFilter (sin JWT).
 * - /api/**       -> JwtAuthenticationFilter (JWT emitido por auth-service).
 *
 * El endpoint GET /api/v1/intereses/catalogo es público (permitAll).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Usado para verificar la contraseña de jurados (credenciales_jurado.
     * password_hash). Los hashes se generan fuera de la app al cargar las
     * credenciales manualmente (ver README: "Jurados").
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain internalFilterChain(HttpSecurity http,
                                                     @Value("${security.internal-api-key}") String internalApiKey)
            throws Exception {
        http.securityMatcher("/internal/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .addFilterBefore(new InternalApiKeyFilter(internalApiKey), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtTokenParser jwtTokenParser) throws Exception {
        http.securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/intereses/catalogo").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenParser), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        // Cubre rutas públicas restantes (Swagger UI, actuator health, etc.).
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/actuator/health/**"
                        ).permitAll()
                        .anyRequest().denyAll());

        return http.build();
    }
}
