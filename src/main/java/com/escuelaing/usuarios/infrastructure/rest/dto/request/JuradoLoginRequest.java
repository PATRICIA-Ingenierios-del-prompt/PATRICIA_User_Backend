package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Contrato interno consumido por auth-service para el login de jurado
 * (correo + contraseña, sin restricción de dominio institucional).
 */
public record JuradoLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
