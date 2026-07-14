package com.escuelaing.usuarios.domain.model;

import java.util.Optional;

/**
 * Categorías de actividad (parches/eventos) tal como las publican
 * parche-service y event-service en sus eventos de RabbitMQ. Se usan para
 * evaluar las reglas de desbloqueo de logros.
 */
public enum CategoriaActividad {
    TECHNOLOGY,
    MUSIC,
    VARIETY,
    ENTERTAINMENT,
    STUDY,
    ART,
    SPORT;

    /**
     * Convierte de forma defensiva el valor de categoría recibido en un
     * mensaje externo. Una categoría desconocida (futura o mal formada) no
     * rompe el consumer: simplemente no dispara ninguna regla de logro.
     */
    public static Optional<CategoriaActividad> fromExterno(String valor) {
        if (valor == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(CategoriaActividad.valueOf(valor.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
