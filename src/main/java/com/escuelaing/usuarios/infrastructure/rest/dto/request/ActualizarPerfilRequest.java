package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;


public record ActualizarPerfilRequest(
        String nombre,
        String apellidos,
        String carrera,
        String segundaCarrera,
        Integer semestre,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaNacimiento,
        Genero genero,
        String foto,
        List<String> intereses,
        Boolean onboardingCompleto,
        @Size(max = 500) String bio,
        Disponibilidad disponibilidad,
        // Opcional: null = no tocar el horario. Presente (incluso lista vacía)
        // = reemplazarlo. Permite guardar el horario desde este mismo PUT si
        // el formulario de perfil lo envía junto con el resto de los campos,
        // en vez de únicamente por PUT /{id}/disponibilidad/horaria.
        @Valid List<FranjaHorariaRequest> franjasDisponibilidad
) {}
