package com.escuelaing.usuarios.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Credencial de acceso para un jurado externo: correo + hash de contraseña,
 * cargados manualmente en la base de datos por un administrador (no hay
 * autorregistro). {@code usuarioId} queda nulo hasta el primer login
 * exitoso, momento en el que se crea el Usuario/Perfil asociado (mismo
 * onboarding que un usuario normal) y se enlaza aquí.
 *
 * POJO de dominio puro: sin anotaciones de JPA ni de ningún framework.
 */
public class CredencialJurado {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private UUID usuarioId;
    private final Instant fechaCreacion;
    private Instant fechaActualizacion;

    private CredencialJurado(UUID id, String email, String passwordHash, UUID usuarioId,
                              Instant fechaCreacion, Instant fechaActualizacion) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.usuarioId = usuarioId;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    /**
     * Reconstruye una credencial existente desde infraestructura.
     */
    public static CredencialJurado reconstruir(UUID id, String email, String passwordHash, UUID usuarioId,
                                                 Instant fechaCreacion, Instant fechaActualizacion) {
        return new CredencialJurado(id, email, passwordHash, usuarioId, fechaCreacion, fechaActualizacion);
    }

    /**
     * Enlaza esta credencial con el Usuario creado en su primer login (o lo
     * reemplaza si el usuario previamente enlazado ya no existe).
     */
    public void enlazarUsuario(UUID usuarioId) {
        this.usuarioId = usuarioId;
        this.fechaActualizacion = Instant.now();
    }

    public boolean primerIngreso() {
        return this.usuarioId == null;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }
}
