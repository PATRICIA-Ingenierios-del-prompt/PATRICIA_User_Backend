package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FotoRequest(
        @NotBlank String urlFoto
) {}
