package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoCreadoMensaje;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoParticipanteUnidoMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume los eventos event.created y event.participant.joined del exchange
 * externo event.events, y los procesa contra el motor de reglas de logros.
 * Ambos significan semánticamente "el usuario tuvo actividad en un evento de
 * categoría X", por lo que se delegan al mismo método del caso de uso.
 */
@Component
public class EventoLogroConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventoLogroConsumer.class);

    private final LogroUseCase logroUseCase;

    public EventoLogroConsumer(LogroUseCase logroUseCase) {
        this.logroUseCase = logroUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_LOGROS_EVENTO_CREADO)
    public void onEventoCreado(EventoCreadoMensaje mensaje) {
        log.info("Logro: mensaje recibido de {} (RK {}) para usuario {} — evento={} categoria={}",
                RabbitMqConfig.EXCHANGE_EVENT_EVENTS, RabbitMqConfig.RK_EVENT_CREATED,
                mensaje.ownerId(), mensaje.eventId(), mensaje.category());
        logroUseCase.procesarActividadEvento(mensaje.ownerId(), mensaje.eventId(), mensaje.category());
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_LOGROS_EVENTO_PARTICIPANTE)
    public void onEventoParticipanteUnido(EventoParticipanteUnidoMensaje mensaje) {
        log.info("Logro: mensaje recibido de {} (RK {}) para usuario {} — evento={} categoria={}",
                RabbitMqConfig.EXCHANGE_EVENT_EVENTS, RabbitMqConfig.RK_EVENT_PARTICIPANT_JOINED,
                mensaje.userId(), mensaje.eventId(), mensaje.category());
        logroUseCase.procesarActividadEvento(mensaje.userId(), mensaje.eventId(), mensaje.category());
    }
}
