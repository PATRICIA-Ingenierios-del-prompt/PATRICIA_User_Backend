package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.ParcheCreadoMensaje;
import com.escuelaing.usuarios.infrastructure.messaging.event.ParcheMiembroUnidoMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume los eventos parche.created y parche.member.joined del exchange
 * externo parche.events, y los procesa contra el motor de reglas de logros.
 * Se distinguen porque algunos logros son "solo únete" (ver LogroService).
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
        log.info("Logro: mensaje recibido de {} (RK {}) para usuario {} — parche={} category={}",
                RabbitMqConfig.EXCHANGE_PARCHE_EVENTS, RabbitMqConfig.RK_PARCHE_MEMBER_JOINED,
                mensaje.memberId(), mensaje.parcheId(), mensaje.category());
        logroUseCase.procesarActividadParche(mensaje.memberId(), mensaje.parcheId(), mensaje.category());
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_LOGROS_PARCHE_CREADO)
    public void onParcheCreado(ParcheCreadoMensaje mensaje) {
        log.info("Logro: mensaje recibido de {} (RK {}) para usuario {} — parche={} category={}",
                RabbitMqConfig.EXCHANGE_PARCHE_EVENTS, RabbitMqConfig.RK_PARCHE_CREATED,
                mensaje.ownerId(), mensaje.parcheId(), mensaje.category());
        logroUseCase.procesarParcheCreado(mensaje.ownerId(), mensaje.parcheId(), mensaje.category());
    }
}
