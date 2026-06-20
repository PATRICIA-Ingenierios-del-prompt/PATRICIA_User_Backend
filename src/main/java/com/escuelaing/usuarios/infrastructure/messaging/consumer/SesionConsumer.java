package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.SesionUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.SesionCerradaMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento sesion.cerrada del exchange patricia.sesiones
 * (declarado externamente). Solo registra auditoría.
 */
@Component
public class SesionConsumer {

    private static final Logger log = LoggerFactory.getLogger(SesionConsumer.class);

    private final SesionUseCase sesionUseCase;

    public SesionConsumer(SesionUseCase sesionUseCase) {
        this.sesionUseCase = sesionUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_SESIONES_CERRADAS)
    public void onSesionCerrada(SesionCerradaMensaje mensaje) {
        log.debug("Recibido sesion.cerrada para usuario {}", mensaje.usuarioId());
        sesionUseCase.registrarSesionCerrada(mensaje.usuarioId());
    }
}
