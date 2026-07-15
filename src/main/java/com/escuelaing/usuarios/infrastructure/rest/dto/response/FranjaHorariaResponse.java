package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record FranjaHorariaResponse(
        UUID id,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin
) {}
