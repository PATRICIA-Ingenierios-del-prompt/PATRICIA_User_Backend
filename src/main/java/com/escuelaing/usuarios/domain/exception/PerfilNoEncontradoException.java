package com.escuelaing.usuarios.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando un usuario no tiene perfil asociado (no debería ocurrir
 * en condiciones normales, ya que el perfil se crea junto al usuario).
 * Debe traducirse a HTTP 404 en la capa de presentación.
 */
public class PerfilNoEncontradoException extends DomainException {

    public PerfilNoEncontradoException(UUID usuarioId) {
        super("No existe perfil asociado al usuario " + usuarioId);
    }
}
