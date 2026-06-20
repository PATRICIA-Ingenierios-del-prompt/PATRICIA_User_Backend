package com.escuelaing.usuarios.domain.exception;

/**
 * Se lanza cuando se viola una invariante general del dominio (p. ej. un
 * correo fuera de los dominios institucionales permitidos, o una bio que
 * excede el máximo de caracteres). Debe traducirse a HTTP 400.
 */
public class DominioInvalidoException extends DomainException {

    public DominioInvalidoException(String message) {
        super(message);
    }
}
