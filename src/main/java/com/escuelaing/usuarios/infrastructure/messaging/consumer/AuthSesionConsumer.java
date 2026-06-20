package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.SesionUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.AuthFallidoMensaje;
import com.escuelaing.usuarios.infrastructure.messaging.event.SesionIniciadaMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume eventos del exchange patricia.auth (declarado por auth-service):
 * - sesion.iniciada (cola usuarios.auth-sesiones): actualiza ultimoAcceso.
 * - auth.fallido (cola usuarios.auth-fallidos): solo auditoría.
 */
@Component
public class AuthSesionConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthSesionConsumer.class);

    private final SesionUseCase sesionUseCase;

    public AuthSesionConsumer(SesionUseCase sesionUseCase) {
        this.sesionUseCase = sesionUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_AUTH_SESIONES)
    public void onSesionIniciada(SesionIniciadaMensaje mensaje) {
        log.debug("Recibido sesion.iniciada para usuario {}", mensaje.usuarioId());
        sesionUseCase.registrarSesionIniciada(mensaje.usuarioId());
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_AUTH_FALLIDOS)
    public void onAuthFallido(AuthFallidoMensaje mensaje) {
        log.debug("Recibido auth.fallido para usuario {}", mensaje.usuarioId());
        sesionUseCase.registrarAuthFallido(mensaje.usuarioId(), mensaje.motivo());
    }
}
