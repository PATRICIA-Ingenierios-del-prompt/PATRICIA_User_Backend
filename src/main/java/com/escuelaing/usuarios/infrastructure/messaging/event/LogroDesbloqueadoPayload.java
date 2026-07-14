package com.escuelaing.usuarios.infrastructure.messaging.event;

/**
 * Payload del evento logro.desbloqueado, publicado en el exchange propio
 * patricia.logros dentro de un EventoEnvelope.
 */
public record LogroDesbloqueadoPayload(String codigo, String nombre, int xp, int xpTotalAcumulado) {
}
