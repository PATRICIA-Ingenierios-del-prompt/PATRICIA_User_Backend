package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> detalles
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message, null);
    }

    public static ErrorResponse of(int status, String error, String message, List<String> detalles) {
        return new ErrorResponse(Instant.now(), status, error, message, detalles);
    }
}
