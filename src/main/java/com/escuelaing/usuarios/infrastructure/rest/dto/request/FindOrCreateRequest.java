package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrato interno consumido por auth-service. NO CAMBIAR estructura.
 */
public record FindOrCreateRequest(
        @NotBlank @Email String email,
        @NotBlank String nombre,
        String microsoftId
) {}
