package com.escuelaing.usuarios.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entidad JPA para la tabla perfil_disponibilidad_horaria.
 * Cada fila representa una franja de disponibilidad declarada por el usuario.
 */
@Entity
@Table(name = "perfil_disponibilidad_horaria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FranjaHorariaEntity {

    @Id
    private UUID id;

    @Column(name = "perfil_id", nullable = false)
    private UUID perfilId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 15)
    private DayOfWeek diaSemana;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;
}
