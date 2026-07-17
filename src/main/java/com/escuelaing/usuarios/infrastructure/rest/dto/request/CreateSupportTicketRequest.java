package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSupportTicketRequest(
        String name,

        String email,

        @NotBlank
        @Size(max = 2000)
        String message
) {
}
