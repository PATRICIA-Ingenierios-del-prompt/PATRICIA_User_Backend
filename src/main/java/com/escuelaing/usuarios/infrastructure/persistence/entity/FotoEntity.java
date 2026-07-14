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
 * Entidad JPA de persistencia para Foto (álbum "monas"). Mapea la tabla `fotos`.
 */
@Entity
@Table(name = "fotos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FotoEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "url_foto", nullable = false)
    private String urlFoto;

    @Column(nullable = false)
    private int orden;

    @Column(name = "fecha_subida", nullable = false)
    private Instant fechaSubida;

    @Column(name = "tiene_persona_en_foto", nullable = false)
    @Builder.Default
    private boolean tienePersonaEnFoto = false;
}
