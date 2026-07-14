package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Payload interno del envelope EventoSaliente publicado por
 * matching-service en la RK match.confirmado.
 */
public record MatchConfirmadoPayload(UUID matchId, UUID otroUsuarioId, double scoreTotal) {
}
