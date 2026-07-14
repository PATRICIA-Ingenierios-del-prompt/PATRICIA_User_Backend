package com.escuelaing.usuarios.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Registro de un parche unido/creado por un usuario, para las reglas de
 * logro que necesitan conteo distinto (Mona Estudiosa) o memoria entre
 * eventos en momentos distintos (Mona Tranquila). Mapea la tabla
 * `logros_parches_registrados`. Solo se pobla para categorías STUDY y
 * VARIETY (ver LogroService).
 */
@Entity
@Table(name = "logros_parches_registrados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogroParcheRegistradoEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "parche_id", nullable = false)
    private UUID parcheId;

    @Column(nullable = false)
    private String categoria;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;
}
