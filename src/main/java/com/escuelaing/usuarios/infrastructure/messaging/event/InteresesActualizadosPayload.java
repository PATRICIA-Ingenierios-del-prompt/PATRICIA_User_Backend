package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.List;

public record InteresesActualizadosPayload(
        List<String> intereses
) {}
