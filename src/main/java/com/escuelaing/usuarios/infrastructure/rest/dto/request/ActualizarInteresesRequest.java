package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ActualizarInteresesRequest(
        @NotNull List<String> intereses
) {}
