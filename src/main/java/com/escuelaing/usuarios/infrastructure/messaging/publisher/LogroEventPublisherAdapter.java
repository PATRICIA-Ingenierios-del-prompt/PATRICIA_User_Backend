package com.escuelaing.usuarios.infrastructure.messaging.publisher;

import com.escuelaing.usuarios.domain.model.LogroTipo;
import com.escuelaing.usuarios.domain.port.outbound.LogroEventPublisherPort;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoEnvelope;
import com.escuelaing.usuarios.infrastructure.messaging.event.LogroDesbloqueadoPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador de mensajería que publica los eventos de logros en el exchange
 * propio patricia.logros. Implementa el puerto de salida
 * LogroEventPublisherPort.
 */
@Component
public class LogroEventPublisherAdapter implements LogroEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LogroEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    public LogroEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarLogroDesbloqueado(UUID usuarioId, LogroTipo tipo, int xp, int xpTotalAcumulado) {
        var payload = new LogroDesbloqueadoPayload(tipo.name(), tipo.getNombre(), xp, xpTotalAcumulado);
        EventoEnvelope<LogroDesbloqueadoPayload> evento =
                EventoEnvelope.of(usuarioId, RabbitMqConfig.RK_LOGRO_DESBLOQUEADO, payload);
        log.info("Publicando evento [{}] eventoId={} usuarioId={} logro={}",
                RabbitMqConfig.RK_LOGRO_DESBLOQUEADO, evento.eventoId(), usuarioId, tipo.name());
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_LOGROS, RabbitMqConfig.RK_LOGRO_DESBLOQUEADO, evento);
    }
}
