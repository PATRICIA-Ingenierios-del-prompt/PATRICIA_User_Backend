package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa el mensaje entrante para el evento sesion.cerrada,
 * publicado en el exchange patricia.sesiones.
 */
public record SesionCerradaMensaje(
        UUID usuarioId,
        Instant timestamp
) {}
