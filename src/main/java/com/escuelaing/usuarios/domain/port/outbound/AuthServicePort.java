package com.escuelaing.usuarios.domain.port.outbound;

import java.util.UUID;

/**
 * Puerto de salida para interactuar con auth-service (vía Feign).
 * Se usa para invalidar sesiones cuando un usuario es suspendido o baneado.
 */
public interface AuthServicePort {

    void cerrarSesion(UUID usuarioId);
}
