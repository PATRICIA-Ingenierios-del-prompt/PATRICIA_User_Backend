package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ActualizarPerfilRequest(
        @Size(max = 500) String bio,
        String carrera,
        Integer semestre,
        List<String> intereses,
        Disponibilidad disponibilidad
) {}
