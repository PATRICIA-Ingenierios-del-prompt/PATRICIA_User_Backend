package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Mensaje consumido del exchange externo event.events (RK event.created).
 *
 * Contrato real confirmado contra event-service (EventCreatedEvent):
 * {sourceEventId, eventId, name, ownerId, linkedToParche, category}. Aquí
 * solo se declaran los tres campos que usan las reglas de logro; el resto se
 * ignora al deserializar. Los nombres deben coincidir literalmente con los
 * del publicador: el usuario a acreditar viaja en ownerId, no en usuarioId.
 */
public record EventoCreadoMensaje(UUID eventId, UUID ownerId, String category) {
}
