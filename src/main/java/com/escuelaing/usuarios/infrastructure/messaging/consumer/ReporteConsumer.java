package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.ReporteUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.ReporteEmitidoMensaje;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consume el evento reporte.emitido del exchange patricia.parches
 * (declarado externamente). Incrementa el contador de reportes del usuario
 * y, si se alcanza el umbral configurado, lo suspende.
 */
@Component
public class ReporteConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReporteConsumer.class);

    private final ReporteUseCase reporteUseCase;

    public ReporteConsumer(ReporteUseCase reporteUseCase) {
        this.reporteUseCase = reporteUseCase;
    }

    @RabbitListener(queues = RabbitMqConfig.QUEUE_REPORTES)
    public void onReporteEmitido(ReporteEmitidoMensaje mensaje) {
        log.debug("Recibido reporte.emitido para usuario {}", mensaje.usuarioId());
        reporteUseCase.registrarReporte(mensaje.usuarioId());
    }
}
