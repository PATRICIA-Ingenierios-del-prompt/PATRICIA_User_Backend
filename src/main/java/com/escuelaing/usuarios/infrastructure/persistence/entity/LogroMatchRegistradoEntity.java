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
 * Registro de un match confirmado del usuario, para el conteo de Mona
 * Social (más de 10 matches). Mapea la tabla `logros_matches_registrados`.
 */
@Entity
@Table(name = "logros_matches_registrados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogroMatchRegistradoEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "match_id", nullable = false)
    private UUID matchId;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro;
}
