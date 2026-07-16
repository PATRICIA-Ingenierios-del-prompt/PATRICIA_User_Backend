package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * El frontend manda y espera horaInicio/horaFin en formato "HH:mm" (así es
 * como sale de su grilla semanal). Sin @JsonFormat, Jackson serializa
 * LocalTime con segundos ("08:00:00"), que nunca hace match contra lo que
 * el frontend guardó ("08:00") al repintar las celdas — el horario sí
 * quedaba guardado, pero la grilla nunca mostraba las celdas activas.
 */
public record FranjaHorariaResponse(
        UUID id,
        DayOfWeek diaSemana,
        @JsonFormat(pattern = "HH:mm") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm") LocalTime horaFin
) {}
