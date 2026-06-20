package com.escuelaing.usuarios.domain.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Catálogo cerrado de intereses disponibles en la plataforma PATRICIA.
 * Cualquier interés que un usuario intente registrar debe pertenecer a este
 * catálogo; de lo contrario se debe lanzar InteresInvalidoException.
 */
public enum Interes {

    // MÚSICA
    CONCIERTOS_EN_VIVO("Conciertos en vivo", CategoriaInteres.MUSICA),
    DJ_ELECTRONICA("DJ & Electrónica", CategoriaInteres.MUSICA),
    ROCK_METAL("Rock & Metal", CategoriaInteres.MUSICA),
    REGGAETON_TRAP("Reggaeton & Trap", CategoriaInteres.MUSICA),
    POP_INDIE("Pop & Indie", CategoriaInteres.MUSICA),
    CLASICA_JAZZ("Clásica & Jazz", CategoriaInteres.MUSICA),

    // ESTUDIO & ACADÉMICO
    GRUPOS_DE_ESTUDIO("Grupos de estudio", CategoriaInteres.ESTUDIO_ACADEMICO),
    TUTORIAS("Tutorías", CategoriaInteres.ESTUDIO_ACADEMICO),
    HACKATHONES("Hackathones", CategoriaInteres.ESTUDIO_ACADEMICO),
    TALLERES_TECNICOS("Talleres técnicos", CategoriaInteres.ESTUDIO_ACADEMICO),
    PRESENTACIONES("Presentaciones", CategoriaInteres.ESTUDIO_ACADEMICO),
    BIBLIOTECA_NOCTURNA("Biblioteca nocturna", CategoriaInteres.ESTUDIO_ACADEMICO),

    // DEPORTE & FITNESS
    BASQUETBOL("Básquetbol", CategoriaInteres.DEPORTE_FITNESS),
    TENIS("Tenis / Tenis de mesa", CategoriaInteres.DEPORTE_FITNESS),
    GIMNASIO("Gimnasio", CategoriaInteres.DEPORTE_FITNESS),
    FUTBOL_DEPORTES("Fútbol & Deportes", CategoriaInteres.DEPORTE_FITNESS),
    YOGA_MEDITACION("Yoga & Meditación", CategoriaInteres.DEPORTE_FITNESS),
    CICLISMO("Ciclismo", CategoriaInteres.DEPORTE_FITNESS),
    PARQUES_NATURALEZA("Parques & Naturaleza", CategoriaInteres.DEPORTE_FITNESS),
    ACTIVIDADES_EXTREMAS("Actividades extremas", CategoriaInteres.DEPORTE_FITNESS),

    // GASTRONOMÍA & SOCIAL
    COMER_EN_CAMPUS("Comer en campus", CategoriaInteres.GASTRONOMIA_SOCIAL),
    FOOD_TRUCKS("Food trucks", CategoriaInteres.GASTRONOMIA_SOCIAL),
    CAFETERIAS_OCULTAS("Cafeterías ocultas", CategoriaInteres.GASTRONOMIA_SOCIAL),
    COMIDA_INTERNACIONAL("Comida internacional", CategoriaInteres.GASTRONOMIA_SOCIAL),
    PICNICS_ASADOS("Picnics & Asados", CategoriaInteres.GASTRONOMIA_SOCIAL),
    INTERCAMBIO_DE_RECETAS("Intercambio de recetas", CategoriaInteres.GASTRONOMIA_SOCIAL),

    // TECNOLOGÍA & GAMING
    VIDEOJUEGOS_COMPETITIVOS("Videojuegos competitivos", CategoriaInteres.TECNOLOGIA_GAMING),
    HACKATHONES_DE_CODIGO("Hackathones de código", CategoriaInteres.TECNOLOGIA_GAMING),
    DESARROLLO_WEB_APP("Desarrollo web/app", CategoriaInteres.TECNOLOGIA_GAMING),
    IA_MACHINE_LEARNING("IA & Machine Learning", CategoriaInteres.TECNOLOGIA_GAMING),
    STREAMING_CONTENT("Streaming & Content", CategoriaInteres.TECNOLOGIA_GAMING),
    ROBOTICA("Robótica", CategoriaInteres.TECNOLOGIA_GAMING),

    // ARTE & CULTURA
    EXPOSICIONES("Exposiciones", CategoriaInteres.ARTE_CULTURA),
    CINE_PELICULAS("Cine & Películas", CategoriaInteres.ARTE_CULTURA),
    TEATRO_DANZA("Teatro & Danza", CategoriaInteres.ARTE_CULTURA),
    FOTOGRAFIA("Fotografía", CategoriaInteres.ARTE_CULTURA),
    MURALES_STREET_ART("Murales & Street art", CategoriaInteres.ARTE_CULTURA),
    LITERATURA_POESIA("Literatura & Poesía", CategoriaInteres.ARTE_CULTURA),

    // COMPETENCIAS & RETOS
    COMPETENCIAS_DEPORTIVAS("Competencias deportivas", CategoriaInteres.COMPETENCIAS_RETOS),
    CONCURSOS_ACADEMICOS("Concursos académicos", CategoriaInteres.COMPETENCIAS_RETOS),
    TORNEOS_DE_JUEGOS("Torneos de juegos", CategoriaInteres.COMPETENCIAS_RETOS),
    DESAFIOS_DE_INNOVACION("Desafíos de innovación", CategoriaInteres.COMPETENCIAS_RETOS),
    MARATONES_DE_PROGRAMACION("Maratones de programación", CategoriaInteres.COMPETENCIAS_RETOS),
    COMPETENCIAS_DE_EMPRENDIMIENTO("Competencias de emprendimiento", CategoriaInteres.COMPETENCIAS_RETOS),

    // PROFESIONAL & NETWORKING
    CHARLAS_DE_EMPRESAS("Charlas de empresas", CategoriaInteres.PROFESIONAL_NETWORKING),
    FERIAS_DE_EMPLEO("Ferias de empleo", CategoriaInteres.PROFESIONAL_NETWORKING),
    MENTORIAS("Mentorías", CategoriaInteres.PROFESIONAL_NETWORKING),
    GRUPOS_PROFESIONALES("Grupos profesionales", CategoriaInteres.PROFESIONAL_NETWORKING),
    CONFERENCIAS("Conferencias", CategoriaInteres.PROFESIONAL_NETWORKING),
    NETWORKING_EVENTS("Networking events", CategoriaInteres.PROFESIONAL_NETWORKING),

    // SOSTENIBILIDAD & CAUSA SOCIAL
    RECICLAJE_ECOLOGIA("Reciclaje & Ecología", CategoriaInteres.SOSTENIBILIDAD_CAUSA_SOCIAL),
    VOLUNTARIADO("Voluntariado", CategoriaInteres.SOSTENIBILIDAD_CAUSA_SOCIAL),
    PROYECTOS_SOCIALES("Proyectos sociales", CategoriaInteres.SOSTENIBILIDAD_CAUSA_SOCIAL),
    DERECHOS_HUMANOS("Derechos humanos", CategoriaInteres.SOSTENIBILIDAD_CAUSA_SOCIAL),
    COMUNIDAD_LGBTQ("Comunidad LGBTQ+", CategoriaInteres.SOSTENIBILIDAD_CAUSA_SOCIAL),
    INICIATIVAS_CAMPESINAS("Iniciativas campesinas", CategoriaInteres.SOSTENIBILIDAD_CAUSA_SOCIAL),

    // VIAJES & AVENTURA
    ROAD_TRIPS("Road trips", CategoriaInteres.VIAJES_AVENTURA),
    VIAJES_INTERNACIONALES("Viajes internacionales", CategoriaInteres.VIAJES_AVENTURA),
    INTERCAMBIOS_ACADEMICOS("Intercambios académicos", CategoriaInteres.VIAJES_AVENTURA),
    BACKPACKING("Backpacking", CategoriaInteres.VIAJES_AVENTURA),
    VIAJES_A_PUEBLOS("Viajes a pueblos", CategoriaInteres.VIAJES_AVENTURA),
    EXPERIENCIAS_RURALES("Experiencias rurales", CategoriaInteres.VIAJES_AVENTURA),

    // BIENESTAR & SALUD MENTAL
    MEDITACION("Meditación", CategoriaInteres.BIENESTAR_SALUD_MENTAL),
    TERAPIA_APOYO("Terapia & Apoyo", CategoriaInteres.BIENESTAR_SALUD_MENTAL),
    NUTRICION("Nutrición", CategoriaInteres.BIENESTAR_SALUD_MENTAL),
    SUENO_DESCANSO("Sueño & Descanso", CategoriaInteres.BIENESTAR_SALUD_MENTAL),
    MINDFULNESS("Mindfulness", CategoriaInteres.BIENESTAR_SALUD_MENTAL),
    COMUNIDAD_DE_BIENESTAR("Comunidad de bienestar", CategoriaInteres.BIENESTAR_SALUD_MENTAL);

    private final String etiqueta;
    private final CategoriaInteres categoria;

    Interes(String etiqueta, CategoriaInteres categoria) {
        this.etiqueta = etiqueta;
        this.categoria = categoria;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public CategoriaInteres getCategoria() {
        return categoria;
    }

    /**
     * Busca un interés del catálogo a partir de su etiqueta visible
     * (p. ej. "Conciertos en vivo"). No distingue mayúsculas/minúsculas.
     */
    public static Optional<Interes> fromEtiqueta(String etiqueta) {
        if (etiqueta == null) {
            return Optional.empty();
        }
        String normalizado = etiqueta.trim();
        return Arrays.stream(values())
                .filter(i -> i.etiqueta.equalsIgnoreCase(normalizado))
                .findFirst();
    }

    public static boolean existe(String etiqueta) {
        return fromEtiqueta(etiqueta).isPresent();
    }
}
