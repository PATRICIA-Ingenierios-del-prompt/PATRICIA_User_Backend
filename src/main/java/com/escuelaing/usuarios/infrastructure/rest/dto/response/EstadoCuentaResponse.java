package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.time.Instant;

public record EstadoCuentaResponse(
        boolean pendienteEliminacion,
        Instant fechaSolicitudEliminacion
) {}
