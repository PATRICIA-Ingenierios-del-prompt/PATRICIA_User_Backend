package com.escuelaing.usuarios.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Franja horaria de disponibilidad declarada por el usuario.
 * Representa un bloque de tiempo en un día de la semana en el que
 * el usuario está disponible para actividades o encuentros.
 *
 * Ejemplo: LUNES 08:00 – 10:00
 */
public class FranjaHoraria {

    private UUID id;
    private UUID perfilId;
    private DayOfWeek diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;

    protected FranjaHoraria() {}

    private FranjaHoraria(UUID id, UUID perfilId, DayOfWeek diaSemana,
                           LocalTime horaInicio, LocalTime horaFin) {
        validar(diaSemana, horaInicio, horaFin);
        this.id = id;
        this.perfilId = perfilId;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public static FranjaHoraria crear(UUID perfilId, DayOfWeek diaSemana,
                                      LocalTime horaInicio, LocalTime horaFin) {
        return new FranjaHoraria(UUID.randomUUID(), perfilId, diaSemana, horaInicio, horaFin);
    }

    public static FranjaHoraria reconstruir(UUID id, UUID perfilId, DayOfWeek diaSemana,
                                             LocalTime horaInicio, LocalTime horaFin) {
        return new FranjaHoraria(id, perfilId, diaSemana, horaInicio, horaFin);
    }

    private static void validar(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        Objects.requireNonNull(diaSemana, "El día de la semana es obligatorio");
        Objects.requireNonNull(horaInicio, "La hora de inicio es obligatoria");
        Objects.requireNonNull(horaFin, "La hora de fin es obligatoria");
        if (!horaInicio.isBefore(horaFin)) {
            throw new com.escuelaing.usuarios.domain.exception.DominioInvalidoException(
                    "La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    public UUID getId()           { return id; }
    public UUID getPerfilId()     { return perfilId; }
    public DayOfWeek getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin()    { return horaFin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FranjaHoraria f)) return false;
        return Objects.equals(id, f.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
