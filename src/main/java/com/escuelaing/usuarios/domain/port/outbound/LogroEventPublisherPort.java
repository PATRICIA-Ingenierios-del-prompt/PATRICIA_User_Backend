package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.LogroTipo;

import java.util.UUID;

/**
 * Puerto de salida para la publicación de eventos de dominio relacionados
 * con logros. Implementado por un adaptador RabbitMQ en
 * infrastructure.messaging, sobre el exchange propio patricia.logros.
 */
public interface LogroEventPublisherPort {

    /**
     * Evento publicado cuando un usuario desbloquea un logro.
     * Routing key: logro.desbloqueado
     */
    void publicarLogroDesbloqueado(UUID usuarioId, LogroTipo tipo, int xp, int xpTotalAcumulado);
}
