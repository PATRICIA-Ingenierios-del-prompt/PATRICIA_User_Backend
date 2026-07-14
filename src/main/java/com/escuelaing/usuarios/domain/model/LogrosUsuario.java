package com.escuelaing.usuarios.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Vista completa del álbum de logros de un usuario: las 13 entradas del
 * catálogo (desbloqueadas o no) más el XP total acumulado.
 */
public record LogrosUsuario(UUID usuarioId, int xpTotal, List<Logro> logros) {
}
