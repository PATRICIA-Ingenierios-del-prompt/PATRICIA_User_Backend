package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.MatchConfirmadoEnvelopeMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento match.confirmado del exchange externo
 * patricia.matching. matching-service publica un mensaje por cada usuario
 * del match (dos mensajes por match confirmado); el usuarioId del nivel
 * superior del envelope ya indica a quién acreditarle Mona Social, por lo
 * que cada mensaje se procesa una sola vez.
 */
@Component
public class MatchLogroConsumer {

    private static final Logger log = LoggerFactory.getLogger(MatchLogroConsumer.class);

    private final LogroUseCase logroUseCase;

    public MatchLogroConsumer(LogroUseCase logroUseCase) {
        this.logroUseCase = logroUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_LOGROS_MATCH_CONFIRMADO)
    public void onMatchConfirmado(MatchConfirmadoEnvelopeMensaje mensaje) {
        log.debug("Recibido match.confirmado usuario={} match={}", mensaje.usuarioId(), mensaje.payload().matchId());
        logroUseCase.procesarMatchConfirmado(mensaje.usuarioId(), mensaje.payload().matchId());
    }
}
