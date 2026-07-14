package com.escuelaing.usuarios.domain.model;

import java.time.Instant;

/**
 * Estado de un logro del catálogo para un usuario concreto: si está
 * desbloqueado y, de ser así, cuándo.
 */
public record Logro(LogroTipo tipo, boolean desbloqueado, Instant fechaDesbloqueo) {
}
