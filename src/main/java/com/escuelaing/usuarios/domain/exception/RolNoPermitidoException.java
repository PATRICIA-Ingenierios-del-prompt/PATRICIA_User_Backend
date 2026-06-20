package com.escuelaing.usuarios.domain.exception;

/**
 * Se lanza cuando un actor sin permisos suficientes intenta modificar roles
 * de un usuario. Debe traducirse a HTTP 403 en la capa de presentación.
 */
public class RolNoPermitidoException extends DomainException {

    public RolNoPermitidoException(String message) {
        super(message);
    }
}
