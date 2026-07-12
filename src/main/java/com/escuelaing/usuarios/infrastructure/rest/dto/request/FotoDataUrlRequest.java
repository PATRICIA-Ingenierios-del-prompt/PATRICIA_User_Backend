package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo JSON para subir una foto del álbum como data-URL base64.
 * Formato esperado: {@code data:image/<tipo>;base64,<contenido>}
 */
public record FotoDataUrlRequest(
        @NotBlank String dataUrl
) {}
