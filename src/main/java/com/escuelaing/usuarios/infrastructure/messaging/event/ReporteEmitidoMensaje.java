package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa el mensaje entrante para el evento reporte.emitido,
 * publicado por el servicio de parches en el exchange patricia.parches.
 */
public record ReporteEmitidoMensaje(
        UUID usuarioId,
        String motivo,
        Instant timestamp
) {}
