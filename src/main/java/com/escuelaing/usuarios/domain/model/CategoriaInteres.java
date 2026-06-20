package com.escuelaing.usuarios.domain.model;

/**
 * Categorías del catálogo de intereses.
 */
public enum CategoriaInteres {
    MUSICA("Música"),
    ESTUDIO_ACADEMICO("Estudio & Académico"),
    DEPORTE_FITNESS("Deporte & Fitness"),
    GASTRONOMIA_SOCIAL("Gastronomía & Social"),
    TECNOLOGIA_GAMING("Tecnología & Gaming"),
    ARTE_CULTURA("Arte & Cultura"),
    COMPETENCIAS_RETOS("Competencias & Retos"),
    PROFESIONAL_NETWORKING("Profesional & Networking"),
    SOSTENIBILIDAD_CAUSA_SOCIAL("Sostenibilidad & Causa social"),
    VIAJES_AVENTURA("Viajes & Aventura"),
    BIENESTAR_SALUD_MENTAL("Bienestar & Salud mental");

    private final String etiqueta;

    CategoriaInteres(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
