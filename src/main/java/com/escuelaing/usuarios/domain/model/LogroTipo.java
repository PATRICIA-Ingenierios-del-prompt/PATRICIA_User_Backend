package com.escuelaing.usuarios.domain.model;

/**
 * Catálogo cerrado de los 13 logros ("monas") del álbum de PATRICIA. Esta es
 * la fuente de verdad de nombre/descripción/XP de cada logro; la tabla SQL
 * logros_catalogo es solo una copia de reporting (mismo patrón que
 * {@link Interes} / intereses_catalogo).
 */
public enum LogroTipo {

    MONA_CODER("Mona Coder", "Únete a un parche de Tecnología", 50),
    MONA_DJ("Mona DJ", "Crea o únete a un parche o evento de Música", 50),
    MONA_CIENTIFICA("Mona Científica", "Crea o únete a un evento de Variedad", 50),
    MONA_CULTURA("Mona Cultura", "Crea o únete a un parche o evento de Entretenimiento", 50),
    MONA_TRANQUILA("Mona Tranquila",
            "Crea o únete a un parche de Variedad y completa 3 ejercicios de bienestar", 100),
    MONA_RESPIRA("Mona Respira", "Completa 3 ejercicios de bienestar", 75),
    MONA_MUSICA("Mona Música", "Crea o únete a un parche o evento de Música", 50),
    MONA_GAMER("Mona Gamer", "Únete a un parche de Entretenimiento", 50),
    MONA_ESTUDIOSA("Mona Estudiosa", "Únete a 3 parches distintos de Estudio", 100),
    MONA_ARTE("Mona Arte", "Crea o únete a un parche o evento de Arte", 50),
    MONA_AIRE_LIBRE("Mona Aire Libre", "Únete a un parche de Deporte", 50),
    MONA_FOODIE("Mona Foodie", "Crea o únete a un parche de Variedad", 50),
    MONA_SOCIAL("Mona Social", "Consigue más de 10 matches confirmados", 100);

    private final String nombre;
    private final String descripcion;
    private final int xp;

    LogroTipo(String nombre, String descripcion, int xp) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.xp = xp;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getXp() {
        return xp;
    }
}
