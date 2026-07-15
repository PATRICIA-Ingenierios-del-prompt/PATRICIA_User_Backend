package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Franja horaria individual dentro del request de disponibilidad.
 * Ejemplo JSON:
 * <pre>
 * { "diaSemana": "MONDAY", "horaInicio": "08:00", "horaFin": "10:00" }
 * </pre>
 */
public record FranjaHorariaRequest(
        @NotNull DayOfWeek diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFin
) {}
