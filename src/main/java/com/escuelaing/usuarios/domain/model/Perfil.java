package com.escuelaing.usuarios.domain.model;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.exception.InteresInvalidoException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Perfil de usuario (relación 1:1 con Usuario). Contiene información social
 * y académica, así como la lista de intereses validados contra el catálogo.
 */
public class Perfil {

    private static final int BIO_MAX_LENGTH = 500;

    private UUID id;
    private UUID usuarioId;
    private String bio;
    private String carrera;
    private Integer semestre;
    private List<String> intereses;
    private Disponibilidad disponibilidad;
    private String urlFotoPerfil;
    private Instant fechaActualizacion;

    protected Perfil() {
        // Para reconstrucción desde infraestructura.
    }

    private Perfil(UUID id, UUID usuarioId, String bio, String carrera, Integer semestre,
                    List<String> intereses, Disponibilidad disponibilidad, String urlFotoPerfil,
                    Instant fechaActualizacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.bio = bio;
        this.carrera = carrera;
        this.semestre = semestre;
        this.intereses = intereses;
        this.disponibilidad = disponibilidad;
        this.urlFotoPerfil = urlFotoPerfil;
        this.fechaActualizacion = fechaActualizacion;
    }

    public static Perfil crearVacio(UUID usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        return new Perfil(UUID.randomUUID(), usuarioId, null, null, null,
                new ArrayList<>(), Disponibilidad.DISPONIBLE, null, Instant.now());
    }

    public static Perfil reconstruir(UUID id, UUID usuarioId, String bio, String carrera,
                                      Integer semestre, List<String> intereses,
                                      Disponibilidad disponibilidad, String urlFotoPerfil,
                                      Instant fechaActualizacion) {
        return new Perfil(id, usuarioId, bio, carrera, semestre,
                intereses == null ? new ArrayList<>() : new ArrayList<>(intereses),
                disponibilidad, urlFotoPerfil, fechaActualizacion);
    }

    /**
     * Actualiza los campos editables del perfil.
     *
     * @return la lista de nombres de campos que efectivamente cambiaron.
     */
    public List<String> actualizar(String bio, String carrera, Integer semestre,
                                    List<String> intereses, Disponibilidad disponibilidad) {
        List<String> camposModificados = new ArrayList<>();

        if (bio != null && bio.length() > BIO_MAX_LENGTH) {
            throw new DominioInvalidoException("La bio no puede superar los " + BIO_MAX_LENGTH + " caracteres");
        }
        if (!Objects.equals(this.bio, bio)) {
            this.bio = bio;
            camposModificados.add("bio");
        }
        if (!Objects.equals(this.carrera, carrera)) {
            this.carrera = carrera;
            camposModificados.add("carrera");
        }
        if (!Objects.equals(this.semestre, semestre)) {
            this.semestre = semestre;
            camposModificados.add("semestre");
        }
        if (intereses != null) {
            validarIntereses(intereses);
            if (!Objects.equals(this.intereses, intereses)) {
                this.intereses = new ArrayList<>(intereses);
                camposModificados.add("intereses");
            }
        }
        if (disponibilidad != null && !Objects.equals(this.disponibilidad, disponibilidad)) {
            this.disponibilidad = disponibilidad;
            camposModificados.add("disponibilidad");
        }

        if (!camposModificados.isEmpty()) {
            this.fechaActualizacion = Instant.now();
        }
        return camposModificados;
    }

    /**
     * Actualiza únicamente los intereses, validándolos contra el catálogo.
     *
     * @return true si la lista de intereses cambió.
     */
    public boolean actualizarIntereses(List<String> nuevosIntereses) {
        validarIntereses(nuevosIntereses);
        if (Objects.equals(this.intereses, nuevosIntereses)) {
            return false;
        }
        this.intereses = new ArrayList<>(nuevosIntereses);
        this.fechaActualizacion = Instant.now();
        return true;
    }

    /**
     * Cambia exclusivamente la disponibilidad.
     *
     * @return true si el valor cambió.
     */
    public boolean cambiarDisponibilidad(Disponibilidad nuevaDisponibilidad) {
        Objects.requireNonNull(nuevaDisponibilidad, "La disponibilidad no puede ser nula");
        if (this.disponibilidad == nuevaDisponibilidad) {
            return false;
        }
        this.disponibilidad = nuevaDisponibilidad;
        this.fechaActualizacion = Instant.now();
        return true;
    }

    public void actualizarUrlFotoPerfil(String urlFotoPerfil) {
        this.urlFotoPerfil = urlFotoPerfil;
        this.fechaActualizacion = Instant.now();
    }

    private void validarIntereses(List<String> intereses) {
        for (String interes : intereses) {
            if (!Interes.existe(interes)) {
                throw new InteresInvalidoException(interes);
            }
        }
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getBio() {
        return bio;
    }

    public String getCarrera() {
        return carrera;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public List<String> getIntereses() {
        return List.copyOf(intereses);
    }

    public Disponibilidad getDisponibilidad() {
        return disponibilidad;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Perfil perfil)) return false;
        return Objects.equals(id, perfil.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
