package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.LogroTipo;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de los logros efectivamente
 * desbloqueados por un usuario (tabla logros_usuario).
 */
public interface LogroRepositoryPort {

    boolean estaDesbloqueado(UUID usuarioId, LogroTipo tipo);

    /**
     * Otorga el logro si el usuario todavía no lo tiene. Es la operación
     * idempotente que protege contra reentregas de RabbitMQ: la constraint
     * única (usuario_id, codigo) hace que una segunda invocación para el
     * mismo logro no inserte una fila duplicada.
     *
     * @return true si esta invocación efectivamente otorgó el logro
     *         (inserción nueva); false si el usuario ya lo tenía.
     */
    boolean otorgarSiNoExiste(UUID usuarioId, LogroTipo tipo, int xp);

    Map<LogroTipo, Instant> buscarDesbloqueadosConFecha(UUID usuarioId);

    int calcularXpTotal(UUID usuarioId);
}
