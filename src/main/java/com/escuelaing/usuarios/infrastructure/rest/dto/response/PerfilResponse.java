package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.escuelaing.usuarios.domain.model.Disponibilidad;

import java.util.List;
import java.util.UUID;

public record PerfilResponse(
        UUID id,
        UUID usuarioId,
        String bio,
        String carrera,
        Integer semestre,
        List<String> intereses,
        Disponibilidad disponibilidad,
        String urlFotoPerfil
) {}
