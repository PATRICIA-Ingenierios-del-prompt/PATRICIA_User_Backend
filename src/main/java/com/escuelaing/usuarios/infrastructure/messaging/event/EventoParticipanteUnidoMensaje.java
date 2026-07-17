package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Mensaje consumido del exchange externo event.events (RK
 * event.participant.joined).
 *
 * Contrato real confirmado contra event-service (ParticipantJoinedEvent):
 * {eventId, userId, category, joinedAt}. Aquí solo se declaran los tres
 * campos que usan las reglas de logro; joinedAt se ignora al deserializar.
 * Ojo: el usuario viaja en userId, mientras que en event.created viaja en
 * ownerId — por eso los dos mensajes son records distintos.
 */
public record EventoParticipanteUnidoMensaje(UUID eventId, UUID userId, String category) {
}
