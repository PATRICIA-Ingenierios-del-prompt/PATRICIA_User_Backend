package com.escuelaing.usuarios.infrastructure.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Respuesta de GET /api/bienestar/usuarios/{id}/ejercicios/count en
 * LLM-Backend: {"userId": "...", "total": N}. Solo se mapea "total"; se
 * ignora "userId" (no se necesita, ya lo sabemos por el path variable).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConteoEjerciciosResponse(int total) {
}
