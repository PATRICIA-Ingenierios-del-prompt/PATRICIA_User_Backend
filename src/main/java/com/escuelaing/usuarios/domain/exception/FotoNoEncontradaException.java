package com.escuelaing.usuarios.domain.exception;

import java.util.UUID;

/**
 * Se lanza cuando se intenta modificar/eliminar una foto que no existe o
 * no pertenece al usuario indicado.
 * Debe traducirse a HTTP 404 en la capa de presentación.
 */
public class FotoNoEncontradaException extends DomainException {

    public FotoNoEncontradaException(UUID fotoId) {
        super("No existe la foto con id " + fotoId);
    }
}
