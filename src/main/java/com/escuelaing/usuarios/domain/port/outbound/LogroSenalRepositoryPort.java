package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.CategoriaActividad;

import java.util.UUID;

/**
 * Puerto de salida para el registro de "señales" de actividad que alimentan
 * reglas de logro que necesitan conteo o memoria entre eventos distintos en
 * el tiempo (Mona Estudiosa, Mona Tranquila, Mona Social). Las reglas de
 * existencia simple no necesitan este puerto: les basta con
 * {@link LogroRepositoryPort#otorgarSiNoExiste}.
 */
public interface LogroSenalRepositoryPort {

    /**
     * Registra que el usuario se unió/creó un parche de una categoría
     * relevante para el conteo (STUDY, VARIETY). Idempotente: una segunda
     * llamada para el mismo (usuarioId, parcheId) no crea una fila nueva.
     *
     * @return true si fue un registro nuevo.
     */
    boolean registrarParche(UUID usuarioId, UUID parcheId, CategoriaActividad categoria);

    long contarParchesDistintosPorCategoria(UUID usuarioId, CategoriaActividad categoria);

    boolean existeParcheCategoria(UUID usuarioId, CategoriaActividad categoria);

    /**
     * Registra un match confirmado del usuario. Idempotente: una segunda
     * llamada para el mismo (usuarioId, matchId) no crea una fila nueva.
     *
     * @return true si fue un registro nuevo.
     */
    boolean registrarMatch(UUID usuarioId, UUID matchId);

    long contarMatches(UUID usuarioId);
}
