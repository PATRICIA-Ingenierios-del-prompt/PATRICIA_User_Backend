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
 * Entidad JPA de persistencia para CredencialJurado. Mapea la tabla
 * `credenciales_jurado`. Objeto de infraestructura sin lógica de negocio.
 */
@Entity
@Table(name = "credenciales_jurado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CredencialJuradoEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "usuario_id", unique = true)
    private UUID usuarioId;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private Instant fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;
}
