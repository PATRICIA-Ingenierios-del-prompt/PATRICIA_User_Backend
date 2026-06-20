package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa el mensaje entrante para el evento auth.fallido,
 * publicado por auth-service en el exchange patricia.auth.
 */
public record AuthFallidoMensaje(
        UUID usuarioId,
        String motivo,
        Instant timestamp
) {}
