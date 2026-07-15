package com.escuelaing.usuarios.domain.port.outbound;

/**
 * Puerto de salida para el módulo de detección de personas en fotos.
 * La implementación delega en el sidecar Python (DeepFace / RetinaFace)
 * que corre en localhost:8090 dentro del mismo contenedor.
 */
public interface PersonaDetectorPort {

    /**
     * Analiza la imagen en {@code urlFoto} y determina si contiene una persona.
     *
     * @param urlFoto URL pública de la imagen (S3 o similar).
     * @return {@code true} si se detectó una persona con confianza suficiente.
     */
    boolean tienPersona(String urlFoto);
}
