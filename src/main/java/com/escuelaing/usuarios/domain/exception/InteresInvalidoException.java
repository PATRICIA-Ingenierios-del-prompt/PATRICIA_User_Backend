package com.escuelaing.usuarios.domain.exception;

/**
 * Se lanza cuando un interés no pertenece al catálogo cerrado de PATRICIA.
 * Debe traducirse a HTTP 400 en la capa de presentación.
 */
public class InteresInvalidoException extends DomainException {

    private final String interes;

    public InteresInvalidoException(String interes) {
        super("El interés '" + interes + "' no pertenece al catálogo de PATRICIA");
        this.interes = interes;
    }

    public String getInteres() {
        return interes;
    }
}
