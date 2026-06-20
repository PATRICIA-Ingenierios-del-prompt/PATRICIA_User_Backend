package com.escuelaing.usuarios.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando no se encuentra un usuario por id o email.
 * Debe traducirse a HTTP 404 en la capa de presentación.
 */
public class UsuarioNoEncontradoException extends DomainException {

    public UsuarioNoEncontradoException(UUID id) {
        super("No existe un usuario con id " + id);
    }

    public UsuarioNoEncontradoException(String email) {
        super("No existe un usuario con email " + email);
    }
}
