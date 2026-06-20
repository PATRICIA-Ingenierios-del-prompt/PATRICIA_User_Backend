package com.escuelaing.usuarios.infrastructure.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita el soporte de caché de Spring (usado por PerfilService para
 * cachear lecturas de perfil en Redis, vía spring-boot-starter-data-redis
 * y spring-boot-starter-cache).
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
