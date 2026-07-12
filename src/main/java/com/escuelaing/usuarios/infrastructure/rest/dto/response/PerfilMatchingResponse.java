package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Contrato interno consumido por matching-service vía:
 * <ul>
 *   <li>{@code GET /internal/usuarios/{id}/perfil-matching}</li>
 *   <li>{@code GET /internal/usuarios/candidatos-matching}</li>
 * </ul>
 * Composición de {@code Usuario} (estado) + {@code Perfil} (carrera,
 * semestre, intereses, disponibilidad) — ver
 * {@code UsuarioPerfilMatchingResponse} en el repo de matching-service,
 * cuyos campos este DTO espeja exactamente.
 */
public record PerfilMatchingResponse(
        UUID id,
        String estado,
        List<String> intereses,
        String carrera,
        Integer semestre,
        String disponibilidad
) {
}
