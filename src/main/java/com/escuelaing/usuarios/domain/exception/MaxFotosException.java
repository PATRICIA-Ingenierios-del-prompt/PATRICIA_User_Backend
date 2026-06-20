package com.escuelaing.usuarios.domain.exception;

/**
 * Se lanza cuando se intenta agregar una foto número 7 (o superior) al
 * álbum de un usuario. Debe traducirse a HTTP 409 en la capa de presentación.
 */
public class MaxFotosException extends DomainException {

    public MaxFotosException(int maximo) {
        super("El álbum ya alcanzó el máximo de " + maximo + " fotos permitidas");
    }
}
