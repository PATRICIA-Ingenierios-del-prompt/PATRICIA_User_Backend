package com.escuelaing.usuarios.domain.port.outbound;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida hacia el servicio de bienestar (LLM-Backend), que no
 * publica eventos y debe consultarse por REST.
 */
public interface BienestarPort {

    /**
     * Cantidad de ejercicios de bienestar completados por el usuario.
     * {@link Optional#empty()} si el servicio de bienestar no respondió o
     * falló: el llamador no debe tratar eso como "cero ejercicios", sino
     * simplemente omitir la evaluación hasta el próximo intento.
     */
    Optional<Integer> contarEjerciciosCompletados(UUID usuarioId);
}
