package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FotoResponse(
        UUID id,
        UUID usuarioId,
        String urlFoto,
        int orden,
        Instant fechaSubida
) {}
