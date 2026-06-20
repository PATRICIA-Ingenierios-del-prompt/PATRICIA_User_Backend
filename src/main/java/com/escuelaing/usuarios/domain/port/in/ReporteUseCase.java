package com.escuelaing.usuarios.domain.port.in;

import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para procesar reportes contra usuarios,
 * consumidos desde el exchange patricia.parches (routing key reporte.emitido).
 */
public interface ReporteUseCase {

    /**
     * Incrementa el contador de reportes del usuario. Si alcanza el umbral
     * configurado (MAX_REPORTES_SUSPENSION), suspende al usuario, publica
     * usuario.suspendido y cierra sus sesiones activas vía auth-service.
     */
    void registrarReporte(UUID usuarioId);
}
