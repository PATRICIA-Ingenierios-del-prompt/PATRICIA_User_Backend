package com.escuelaing.usuarios.infrastructure.messaging.event;

import com.escuelaing.usuarios.domain.model.MotivoSuspension;

public record UsuarioSuspendidoPayload(
        MotivoSuspension motivo,
        int cantidadReportes
) {}
