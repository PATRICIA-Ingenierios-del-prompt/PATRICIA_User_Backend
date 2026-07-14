package com.escuelaing.usuarios.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Cliente Feign hacia LLM-Backend (bienestar), usado para evaluar las
 * reglas de logro Mona Respira / Mona Tranquila. Bienestar no publica
 * eventos, así que este servicio lo consulta por REST.
 *
 * Contrato (definido por el equipo de bienestar/LLM-Backend, NO CAMBIAR):
 * GET /api/bienestar/usuarios/{id}/ejercicios/count
 */
@FeignClient(name = "llm-backend", url = "${clients.llm-backend.url}")
public interface BienestarInternalClient {

    @GetMapping("/api/bienestar/usuarios/{id}/ejercicios/count")
    ConteoEjerciciosResponse contarEjercicios(@PathVariable("id") UUID usuarioId);
}
