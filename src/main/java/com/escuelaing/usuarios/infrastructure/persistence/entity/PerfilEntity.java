package com.escuelaing.usuarios.infrastructure.persistence.entity;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidad JPA de persistencia para Perfil. Mapea la tabla `perfiles`,
 * relacionada 1:1 con `usuarios` mediante usuario_id.
 */
@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id", nullable = false, unique = true)
    private UUID usuarioId;

    private String nombre;

    private String apellidos;

    @Column(length = 500)
    private String bio;

    private String carrera;

    @Column(name = "segunda_carrera")
    private String segundaCarrera;

    private Integer semestre;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Genero genero;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_intereses", joinColumns = @JoinColumn(name = "perfil_id"))
    @OrderColumn(name = "posicion")
    @Column(name = "interes", nullable = false)
    @Builder.Default
    private List<String> intereses = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Disponibilidad disponibilidad;

    @Column(name = "url_foto_perfil")
    private String urlFotoPerfil;

    @Column(name = "onboarding_completo", nullable = false)
    @Builder.Default
    private boolean onboardingCompleto = false;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Instant fechaActualizacion;
}
