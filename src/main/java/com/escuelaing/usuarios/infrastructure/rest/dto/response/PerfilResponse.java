package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PerfilResponse(
        UUID id,
        UUID usuarioId,
        String nombre,
        String apellidos,
        String bio,
        String carrera,
        String segundaCarrera,
        Integer semestre,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento,
        Genero genero,
        List<String> intereses,
        Disponibilidad disponibilidad,
        String urlFotoPerfil,
        boolean tienePersonaEnFoto,
        List<FranjaHorariaResponse> franjasDisponibilidad,
        boolean onboardingCompleto
) {}
