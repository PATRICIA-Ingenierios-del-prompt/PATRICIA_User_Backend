package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa el mensaje entrante para el evento sesion.iniciada,
 * publicado por auth-service en el exchange patricia.auth.
 */
public record SesionIniciadaMensaje(
        UUID usuarioId,
        Instant timestamp
) {}
