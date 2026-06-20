package com.escuelaing.usuarios.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

/**
 * Cliente Feign hacia auth-service, usado para invalidar sesiones cuando
 * un usuario es suspendido o baneado.
 *
 * Contrato (definido por auth-service, NO CAMBIAR):
 * POST /internal/auth/cerrar-sesion/{userId}
 * Header: X-Internal-Api-Key
 */
@FeignClient(name = "auth-service", url = "${clients.auth-service.url}")
public interface AuthInternalClient {

    @PostMapping("/internal/auth/cerrar-sesion/{userId}")
    void cerrarSesion(@PathVariable("userId") UUID userId,
                       @RequestHeader("X-Internal-Api-Key") String apiKey);
}
