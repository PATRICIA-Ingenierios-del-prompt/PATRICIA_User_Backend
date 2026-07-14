package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Mensaje consumido del exchange externo event.events (RK event.created).
 *
 * ⚠️ Nombres de campo asumidos siguiendo la convención de mensajes de este
 * repo; confirmar el contrato exacto con event-service antes de desplegar a
 * un ambiente compartido.
 */
public record EventoCreadoMensaje(UUID usuarioId, UUID eventoId, String categoria) {
}
