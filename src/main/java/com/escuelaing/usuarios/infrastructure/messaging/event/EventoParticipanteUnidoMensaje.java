package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Mensaje consumido del exchange externo event.events (RK
 * event.participant.joined).
 *
 * ⚠️ Nombres de campo asumidos siguiendo la convención de mensajes de este
 * repo; confirmar el contrato exacto con event-service antes de desplegar a
 * un ambiente compartido.
 */
public record EventoParticipanteUnidoMensaje(UUID usuarioId, UUID eventoId, String categoria) {
}
