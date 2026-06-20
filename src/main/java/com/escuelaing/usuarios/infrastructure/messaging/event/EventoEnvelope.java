package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Envelope estándar para todos los eventos publicados por usuarios-service
 * en el exchange patricia.usuarios.
 *
 * @param eventoId  identificador único del evento (para idempotencia en consumidores).
 * @param timestamp instante de creación del evento en formato ISO-8601.
 * @param usuarioId identificador del usuario al que refiere el evento.
 * @param tipo      routing key / tipo de evento (p. ej. "usuario.creado").
 * @param payload   cuerpo específico del evento.
 */
public record EventoEnvelope<T>(
        UUID eventoId,
        Instant timestamp,
        UUID usuarioId,
        String tipo,
        T payload
) {
    public static <T> EventoEnvelope<T> of(UUID usuarioId, String tipo, T payload) {
        return new EventoEnvelope<>(UUID.randomUUID(), Instant.now(), usuarioId, tipo, payload);
    }
}
