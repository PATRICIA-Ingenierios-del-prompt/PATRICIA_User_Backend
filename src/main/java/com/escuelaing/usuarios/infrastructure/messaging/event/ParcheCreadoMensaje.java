package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Mensaje consumido del exchange externo parche.events (RK parche.created).
 * Contrato real de ParcheCreatedEvent confirmado por Parches_Backend con
 * captura de mensaje real: {parcheId, ownerId, category}.
 */
public record ParcheCreadoMensaje(UUID parcheId, UUID ownerId, String category) {
}
