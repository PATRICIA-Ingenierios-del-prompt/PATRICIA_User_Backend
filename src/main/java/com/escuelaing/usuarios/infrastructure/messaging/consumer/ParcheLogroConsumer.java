package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.ParcheMiembroUnidoMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento parche.member.joined del exchange externo
 * parche.events, y lo procesa contra el motor de reglas de logros.
 */
@Component
public class ParcheLogroConsumer {

    private static final Logger log = LoggerFactory.getLogger(ParcheLogroConsumer.class);

    private final LogroUseCase logroUseCase;

    public ParcheLogroConsumer(LogroUseCase logroUseCase) {
        this.logroUseCase = logroUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_LOGROS_PARCHE_UNIDO)
    public void onParcheMiembroUnido(ParcheMiembroUnidoMensaje mensaje) {
        log.debug("Recibido parche.member.joined miembro={} parche={} category={}",
                mensaje.memberId(), mensaje.parcheId(), mensaje.category());
        logroUseCase.procesarActividadParche(mensaje.memberId(), mensaje.parcheId(), mensaje.category());
    }
}
