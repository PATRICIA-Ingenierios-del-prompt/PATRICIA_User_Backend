package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.List;

public record CamposModificadosPayload(
        List<String> camposModificados
) {}
