package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.LogrosUsuario;

import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para el álbum de logros de un usuario.
 */
public interface LogroUseCase {

    /**
     * Devuelve el catálogo completo de logros para un usuario (desbloqueados
     * o no) junto con el XP total acumulado. Antes de construir la
     * respuesta, reevalúa de forma perezosa los logros que dependen del
     * conteo de ejercicios de bienestar (Mona Respira / Mona Tranquila).
     */
    LogrosUsuario obtenerLogros(UUID usuarioId);

    /**
     * Procesa una señal de "unirse a un parche" (parche.member.joined) para
     * el motor de reglas de logros.
     */
    void procesarActividadParche(UUID usuarioId, UUID parcheId, String categoria);

    /**
     * Procesa una señal de "crear un parche" (parche.created) para el motor
     * de reglas de logros. Distinta de {@link #procesarActividadParche}
     * porque algunos logros (Mona Coder, Mona Aire Libre) son "solo únete" y
     * no deben otorgarse al crear.
     */
    void procesarParcheCreado(UUID usuarioId, UUID parcheId, String categoria);

    /**
     * Procesa una señal de actividad de evento (event.created o
     * event.participant.joined) para el motor de reglas de logros.
     */
    void procesarActividadEvento(UUID usuarioId, UUID eventoId, String categoria);

    /**
     * Procesa un match confirmado (match.confirmado) para el usuario
     * indicado, hacia el conteo de Mona Social.
     */
    void procesarMatchConfirmado(UUID usuarioId, UUID matchId);
}
