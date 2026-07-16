package com.escuelaing.usuarios.domain.exception;

/**
 * Se lanza cuando el login de jurado (correo + contraseña) falla: correo no
 * registrado en credenciales_jurado o contraseña incorrecta. Se traduce a
 * HTTP 401 en la capa de presentación. Mensaje deliberadamente genérico
 * para no revelar si el correo existe.
 */
public class CredencialesInvalidasException extends DomainException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
