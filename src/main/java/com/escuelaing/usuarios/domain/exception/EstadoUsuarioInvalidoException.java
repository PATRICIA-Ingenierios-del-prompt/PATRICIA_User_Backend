package com.escuelaing.usuarios.domain.exception;

/**
 * Se lanza cuando se intenta realizar una transición de estado no permitida
 * sobre un usuario (p. ej. reactivar a un usuario baneado).
 * Debe traducirse a HTTP 409 en la capa de presentación.
 */
public class EstadoUsuarioInvalidoException extends DomainException {

    public EstadoUsuarioInvalidoException(String message) {
        super(message);
    }
}
