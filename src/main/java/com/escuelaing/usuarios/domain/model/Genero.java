package com.escuelaing.usuarios.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Género declarado por el usuario en el onboarding. Se serializa/deserializa
 * en minúsculas hacia la API pública ("masculino", "femenino", "otro", "nd")
 * pero se persiste como STRING en mayúsculas en base de datos.
 */
public enum Genero {
    MASCULINO,
    FEMENINO,
    OTRO,
    ND;

    @JsonCreator
    public static Genero desdeTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return Genero.valueOf(valor.trim().toUpperCase());
    }

    @JsonValue
    public String haciaTexto() {
        return this.name().toLowerCase();
    }
}
