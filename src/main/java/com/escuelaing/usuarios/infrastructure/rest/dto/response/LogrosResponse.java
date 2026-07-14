package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.util.List;
import java.util.UUID;

public record LogrosResponse(
        UUID usuarioId,
        int xpTotal,
        List<LogroResponse> logros
) {}
