package com.escuelaing.usuarios.domain.model;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.exception.InteresInvalidoException;
import com.escuelaing.usuarios.domain.exception.OnboardingException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Perfil de usuario (relación 1:1 con Usuario). Contiene información social
 * y académica, así como la lista de intereses validados contra el catálogo.
 *
 * El nombre/apellidos que vive aquí es el declarado por el propio usuario
 * durante el onboarding y es independiente del `nombre` de {@link Usuario}
 * (que viene de SSO/Microsoft).
 */
public class Perfil {

    private static final int BIO_MAX_LENGTH = 500;

    private UUID id;
    private UUID usuarioId;
    private String nombre;
    private String apellidos;
    private String bio;
    private String carrera;
    private String segundaCarrera;
    private Integer semestre;
    private LocalDate fechaNacimiento;
    private Genero genero;
    private List<String> intereses;
    private Disponibilidad disponibilidad;
    private String urlFotoPerfil;
    /** Indica si se detectó una persona en la foto de perfil. */
    private boolean tienePersonaEnFoto;
    private List<FranjaHoraria> franjasDisponibilidad;
    private boolean onboardingCompleto;
    private Instant fechaActualizacion;

    protected Perfil() {
        // Para reconstrucción desde infraestructura.
    }

    private Perfil(UUID id, UUID usuarioId, String nombre, String apellidos, String bio, String carrera,
                   String segundaCarrera, Integer semestre, LocalDate fechaNacimiento, Genero genero,
                   List<String> intereses, Disponibilidad disponibilidad, String urlFotoPerfil,
                   boolean tienePersonaEnFoto, List<FranjaHoraria> franjasDisponibilidad,
                   boolean onboardingCompleto, Instant fechaActualizacion) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.bio = bio;
        this.carrera = carrera;
        this.segundaCarrera = segundaCarrera;
        this.semestre = semestre;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.intereses = intereses;
        this.disponibilidad = disponibilidad;
        this.urlFotoPerfil = urlFotoPerfil;
        this.tienePersonaEnFoto = tienePersonaEnFoto;
        this.franjasDisponibilidad = franjasDisponibilidad != null ? new ArrayList<>(franjasDisponibilidad) : new ArrayList<>();
        this.onboardingCompleto = onboardingCompleto;
        this.fechaActualizacion = fechaActualizacion;
    }

    public static Perfil crearVacio(UUID usuarioId) {
        Objects.requireNonNull(usuarioId, "usuarioId no puede ser nulo");
        return new Perfil(UUID.randomUUID(), usuarioId, null, null, null, null, null, null, null, null,
                new ArrayList<>(), Disponibilidad.DISPONIBLE, null, false, new ArrayList<>(), false, Instant.now());
    }

    public static Perfil reconstruir(UUID id, UUID usuarioId, String nombre, String apellidos, String bio,
                                     String carrera, String segundaCarrera, Integer semestre,
                                     LocalDate fechaNacimiento, Genero genero, List<String> intereses,
                                     Disponibilidad disponibilidad, String urlFotoPerfil,
                                     boolean onboardingCompleto, Instant fechaActualizacion) {
        return new Perfil(id, usuarioId, nombre, apellidos, bio, carrera, segundaCarrera, semestre,
                fechaNacimiento, genero,
                intereses == null ? new ArrayList<>() : new ArrayList<>(intereses),
                disponibilidad, urlFotoPerfil, false, new ArrayList<>(), onboardingCompleto, fechaActualizacion);
    }

    public static Perfil reconstruir(UUID id, UUID usuarioId, String nombre, String apellidos, String bio,
                                     String carrera, String segundaCarrera, Integer semestre,
                                     LocalDate fechaNacimiento, Genero genero, List<String> intereses,
                                     Disponibilidad disponibilidad, String urlFotoPerfil,
                                     boolean tienePersonaEnFoto, List<FranjaHoraria> franjasDisponibilidad,
                                     boolean onboardingCompleto, Instant fechaActualizacion) {
        return new Perfil(id, usuarioId, nombre, apellidos, bio, carrera, segundaCarrera, semestre,
                fechaNacimiento, genero,
                intereses == null ? new ArrayList<>() : new ArrayList<>(intereses),
                disponibilidad, urlFotoPerfil, tienePersonaEnFoto, franjasDisponibilidad,
                onboardingCompleto, fechaActualizacion);
    }

    /**
     * Completa el onboarding por primera vez. Es una operación de una sola
     * vez: si el perfil ya estaba marcado como onboarded, lanza
     * {@link OnboardingException}.
     *
     * Los campos nombre, apellidos, carrera, semestre e intereses son
     * obligatorios; segundaCarrera, fechaNacimiento, genero y la foto son
     * opcionales.
     */
    public void completarOnboarding(String nombre, String apellidos, String carrera, String segundaCarrera,
                                    Integer semestre, LocalDate fechaNacimiento, Genero genero,
                                    String urlFotoPerfil, List<String> intereses) {
        if (this.onboardingCompleto) {
            throw new OnboardingException(this.usuarioId);
        }
        if (nombre == null || nombre.isBlank()) {
            throw new DominioInvalidoException("nombre es obligatorio para completar el onboarding");
        }
        if (apellidos == null || apellidos.isBlank()) {
            throw new DominioInvalidoException("apellidos es obligatorio para completar el onboarding");
        }
        if (carrera == null || carrera.isBlank()) {
            throw new DominioInvalidoException("carrera es obligatoria para completar el onboarding");
        }
        if (semestre == null) {
            throw new DominioInvalidoException("semestre es obligatorio para completar el onboarding");
        }
        if (intereses == null || intereses.isEmpty()) {
            throw new DominioInvalidoException("debe seleccionar al menos un interés para completar el onboarding");
        }
        validarIntereses(intereses);

        this.nombre = nombre;
        this.apellidos = apellidos;
        this.carrera = carrera;
        this.segundaCarrera = segundaCarrera;
        this.semestre = semestre;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.intereses = new ArrayList<>(intereses);
        if (urlFotoPerfil != null && !urlFotoPerfil.isBlank()) {
            this.urlFotoPerfil = urlFotoPerfil;
        }
        this.onboardingCompleto = true;
        this.fechaActualizacion = Instant.now();
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

    /**
     * Marca la foto de perfil como "con persona detectada".
     * @return true si el campo cambió (era false), false si ya estaba activo.
     */
    public boolean marcarPersonaDetectadaEnFoto() {
        if (this.tienePersonaEnFoto) return false;
        this.tienePersonaEnFoto = true;
        this.fechaActualizacion = Instant.now();
        return true;
    }

    /**
     * Resetea el flag de persona detectada. Se llama cuando se reemplaza la foto
     * de perfil, para que la nueva foto sea re-verificada.
     */
    public void resetearPersonaEnFoto() {
        this.tienePersonaEnFoto = false;
        this.fechaActualizacion = Instant.now();
    }

    /**
     * Reemplaza completamente las franjas de disponibilidad horaria.
     * Una lista vacía significa "sin franjas declaradas".
     */
    public void actualizarFranjasDisponibilidad(List<FranjaHoraria> nuevasFranjas) {
        this.franjasDisponibilidad = nuevasFranjas == null ? new ArrayList<>() : new ArrayList<>(nuevasFranjas);
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

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public String getBio() {
        return bio;
    }

    public String getCarrera() {
        return carrera;
    }

    public String getSegundaCarrera() {
        return segundaCarrera;
    }

    public Integer getSemestre() {
        return semestre;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public Genero getGenero() {
        return genero;
    }

    public boolean isOnboardingCompleto() {
        return onboardingCompleto;
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

    public boolean isTienePersonaEnFoto() {
        return tienePersonaEnFoto;
    }

    public List<FranjaHoraria> getFranjasDisponibilidad() {
        return List.copyOf(franjasDisponibilidad);
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
