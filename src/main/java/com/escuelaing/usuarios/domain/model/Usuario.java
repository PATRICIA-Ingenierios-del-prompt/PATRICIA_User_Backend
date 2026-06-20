package com.escuelaing.usuarios.domain.model;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.exception.EstadoUsuarioInvalidoException;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Agregado raíz Usuario. Encapsula las reglas de negocio relacionadas con
 * el ciclo de vida del usuario: creación, suspensión, baneo, reportes y roles.
 *
 * Esta clase es un POJO de dominio puro: no tiene anotaciones de JPA, Jackson
 * ni de ningún otro framework. La persistencia y serialización se resuelven
 * en los adaptadores de infraestructura.
 */
public class Usuario {

    private static final Pattern DOMINIO_PERMITIDO =
            Pattern.compile("^[A-Za-z0-9._%+-]+@(mail\\.escuelaing\\.edu\\.co|escuelaing\\.edu\\.co)$");

    private UUID id;
    private String email;
    private String nombre;
    private String microsoftId;
    private EstadoUsuario estado;
    private Set<RolPlataforma> roles;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;
    private Instant ultimoAcceso;
    private int contadorReportes;

    protected Usuario() {
        // Para reconstrucción desde infraestructura (mappers).
    }

    private Usuario(UUID id, String email, String nombre, String microsoftId,
                     EstadoUsuario estado, Set<RolPlataforma> roles,
                     Instant fechaCreacion, Instant fechaActualizacion,
                     Instant ultimoAcceso, int contadorReportes) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.microsoftId = microsoftId;
        this.estado = estado;
        this.roles = roles;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.ultimoAcceso = ultimoAcceso;
        this.contadorReportes = contadorReportes;
    }

    /**
     * Crea un nuevo usuario validando el dominio del correo institucional.
     * Por defecto se asigna el rol STUDENT y estado ACTIVE.
     */
    public static Usuario crearNuevo(String email, String nombre, String microsoftId) {
        validarEmail(email);
        Instant ahora = Instant.now();
        Set<RolPlataforma> rolesIniciales = new HashSet<>();
        rolesIniciales.add(RolPlataforma.STUDENT);
        return new Usuario(
                UUID.randomUUID(),
                email,
                nombre,
                microsoftId,
                EstadoUsuario.ACTIVE,
                rolesIniciales,
                ahora,
                ahora,
                null,
                0
        );
    }

    /**
     * Reconstruye un usuario existente desde infraestructura (persistencia).
     * No aplica validaciones de creación; asume que el estado ya es válido.
     */
    public static Usuario reconstruir(UUID id, String email, String nombre, String microsoftId,
                                       EstadoUsuario estado, Set<RolPlataforma> roles,
                                       Instant fechaCreacion, Instant fechaActualizacion,
                                       Instant ultimoAcceso, int contadorReportes) {
        return new Usuario(id, email, nombre, microsoftId, estado,
                roles == null ? new HashSet<>() : new HashSet<>(roles),
                fechaCreacion, fechaActualizacion, ultimoAcceso, contadorReportes);
    }

    private static void validarEmail(String email) {
        if (email == null || !DOMINIO_PERMITIDO.matcher(email).matches()) {
            throw new DominioInvalidoException(
                    "El correo debe pertenecer a @mail.escuelaing.edu.co o @escuelaing.edu.co");
        }
    }

    /**
     * Actualiza el microsoftId solo si actualmente está vacío (regla de
     * idempotencia de find-or-create).
     *
     * @return true si se modificó el microsoftId.
     */
    public boolean asignarMicrosoftIdSiAusente(String microsoftId) {
        if (microsoftId == null || microsoftId.isBlank()) {
            return false;
        }
        if (this.microsoftId != null && !this.microsoftId.isBlank()) {
            return false;
        }
        this.microsoftId = microsoftId;
        this.fechaActualizacion = Instant.now();
        return true;
    }

    public void registrarAcceso() {
        this.ultimoAcceso = Instant.now();
    }

    public void incrementarReportes() {
        this.contadorReportes++;
    }

    public void asignarRoles(Set<RolPlataforma> nuevosRoles) {
        if (nuevosRoles == null || nuevosRoles.isEmpty()) {
            throw new DominioInvalidoException("Debe asignarse al menos un rol");
        }
        this.roles = EnumSet.copyOf(nuevosRoles);
        this.fechaActualizacion = Instant.now();
    }

    public void suspender() {
        if (this.estado == EstadoUsuario.BANNED) {
            throw new EstadoUsuarioInvalidoException("Un usuario baneado no puede ser suspendido");
        }
        this.estado = EstadoUsuario.SUSPENDED;
        this.fechaActualizacion = Instant.now();
    }

    public void banear() {
        this.estado = EstadoUsuario.BANNED;
        this.fechaActualizacion = Instant.now();
    }

    public void reactivar() {
        if (this.estado == EstadoUsuario.BANNED) {
            throw new EstadoUsuarioInvalidoException("Un usuario baneado no puede reactivarse automáticamente");
        }
        this.estado = EstadoUsuario.ACTIVE;
        this.fechaActualizacion = Instant.now();
    }

    public void cambiarEstado(EstadoUsuario nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "El estado no puede ser nulo");
        this.fechaActualizacion = Instant.now();
    }

    public boolean debeSerSuspendidoPorReportes(int maxReportes) {
        return this.contadorReportes >= maxReportes && this.estado == EstadoUsuario.ACTIVE;
    }

    public boolean esNuevo() {
        return this.fechaCreacion != null && this.fechaCreacion.equals(this.fechaActualizacion)
                && this.ultimoAcceso == null && this.contadorReportes == 0;
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNombre() {
        return nombre;
    }

    public String getMicrosoftId() {
        return microsoftId;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public Set<RolPlataforma> getRoles() {
        return Set.copyOf(roles);
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public Instant getUltimoAcceso() {
        return ultimoAcceso;
    }

    public int getContadorReportes() {
        return contadorReportes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario usuario)) return false;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
