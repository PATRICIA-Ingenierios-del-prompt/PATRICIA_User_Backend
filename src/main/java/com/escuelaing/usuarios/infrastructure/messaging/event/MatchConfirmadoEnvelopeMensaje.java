package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Mensaje consumido del exchange externo patricia.matching (RK
 * match.confirmado). matching-service publica su propio envelope genérico
 * "EventoSaliente" (misma forma que nuestro EventoEnvelope, pero es un tipo
 * distinto: pertenece a matching-service, no se reutiliza el genérico de
 * este servicio para no acoplar contratos de servicios distintos).
 *
 * matching-service publica un mensaje por cada usuario del match
 * confirmado (dos mensajes por match); usuarioId en el nivel superior ya
 * indica a quién acreditarle el logro, sin necesidad de comparar contra
 * nada más.
 */
public record MatchConfirmadoEnvelopeMensaje(
        UUID eventoId,
        Instant timestamp,
        UUID usuarioId,
        String tipo,
        MatchConfirmadoPayload payload
) {
}
