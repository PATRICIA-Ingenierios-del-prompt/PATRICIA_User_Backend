package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoRequest(
        @NotNull EstadoUsuario estado
) {}
