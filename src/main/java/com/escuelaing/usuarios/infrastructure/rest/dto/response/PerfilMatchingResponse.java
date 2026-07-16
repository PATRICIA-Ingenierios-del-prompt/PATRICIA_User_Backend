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
 * semestre, intereses, disponibilidad, foto) — ver
 * {@code UsuarioPerfilMatchingResponse} en el repo de matching-service,
 * cuyos campos este DTO espeja exactamente.
 *
 * urlFotoPerfil/tienePersonaEnFoto/franjasDisponibilidad se agregaron para
 * que matching-service pueda ver la foto y el horario vigentes (antes este
 * contrato no los exponía en absoluto, así que un cambio nunca llegaba a
 * matching sin importar el evento publicado). Requiere los campos espejo
 * correspondientes en matching-service para que el Feign client los
 * deserialice.
 */
public record PerfilMatchingResponse(
        UUID id,
        String estado,
        List<String> intereses,
        String carrera,
        Integer semestre,
        String disponibilidad,
        String urlFotoPerfil,
        boolean tienePersonaEnFoto,
        List<FranjaHorariaResponse> franjasDisponibilidad
) {
}
