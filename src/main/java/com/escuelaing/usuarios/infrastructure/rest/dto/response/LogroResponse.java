package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.time.Instant;

public record LogroResponse(
        String codigo,
        String nombre,
        String descripcion,
        int xp,
        boolean desbloqueado,
        Instant fechaDesbloqueo
) {}
