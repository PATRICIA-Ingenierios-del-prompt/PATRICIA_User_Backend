package com.escuelaing.usuarios.domain.port.in;

import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para procesar eventos de sesión
 * provenientes de auth-service (exchange patricia.auth / patricia.sesiones).
 */
public interface SesionUseCase {

    /**
     * Procesa el evento sesion.iniciada: actualiza el campo ultimoAcceso
     * del usuario correspondiente.
     */
    void registrarSesionIniciada(UUID usuarioId);

    /**
     * Procesa el evento auth.fallido: solo auditoría, no modifica estado.
     */
    void registrarAuthFallido(UUID usuarioId, String motivo);

    /**
     * Procesa el evento sesion.cerrada: solo auditoría.
     */
    void registrarSesionCerrada(UUID usuarioId);
}
