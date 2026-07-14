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
 * Entidad JPA de persistencia para un logro efectivamente desbloqueado por
 * un usuario. Mapea la tabla `logros_usuario`.
 */
@Entity
@Table(name = "logros_usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogroUsuarioEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false)
    private String codigo;

    @Column(name = "xp_otorgado", nullable = false)
    private int xpOtorgado;

    @Column(name = "fecha_desbloqueo", nullable = false)
    private Instant fechaDesbloqueo;
}
