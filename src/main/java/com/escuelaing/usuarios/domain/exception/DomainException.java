package com.escuelaing.usuarios.domain.exception;

/**
 * Excepción raíz para todas las violaciones de reglas de negocio del dominio
 * de usuarios-service.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
