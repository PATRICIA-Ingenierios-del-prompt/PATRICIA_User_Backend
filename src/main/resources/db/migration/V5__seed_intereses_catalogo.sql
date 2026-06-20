-- Tabla de referencia del catálogo cerrado de intereses de PATRICIA.
-- El catálogo "fuente de verdad" para validación en runtime vive en el
-- enum de dominio com.escuelaing.usuarios.domain.model.Interes; esta tabla
-- existe para:
--  (a) permitir consultas SQL directas/reporting sobre el catálogo,
--  (b) servir como referencia para FKs futuras si se requieren,
--  (c) facilitar auditoría de cambios al catálogo a lo largo del tiempo.
CREATE TABLE intereses_catalogo (
    codigo     VARCHAR(100) PRIMARY KEY,
    etiqueta   VARCHAR(150) NOT NULL,
    categoria  VARCHAR(50)  NOT NULL
);

INSERT INTO intereses_catalogo (codigo, etiqueta, categoria) VALUES
-- MÚSICA
('CONCIERTOS_EN_VIVO', 'Conciertos en vivo', 'MUSICA'),
('DJ_ELECTRONICA', 'DJ & Electrónica', 'MUSICA'),
('ROCK_METAL', 'Rock & Metal', 'MUSICA'),
('REGGAETON_TRAP', 'Reggaeton & Trap', 'MUSICA'),
('POP_INDIE', 'Pop & Indie', 'MUSICA'),
('CLASICA_JAZZ', 'Clásica & Jazz', 'MUSICA'),

-- ESTUDIO & ACADÉMICO
('GRUPOS_DE_ESTUDIO', 'Grupos de estudio', 'ESTUDIO_ACADEMICO'),
('TUTORIAS', 'Tutorías', 'ESTUDIO_ACADEMICO'),
('HACKATHONES', 'Hackathones', 'ESTUDIO_ACADEMICO'),
('TALLERES_TECNICOS', 'Talleres técnicos', 'ESTUDIO_ACADEMICO'),
('PRESENTACIONES', 'Presentaciones', 'ESTUDIO_ACADEMICO'),
('BIBLIOTECA_NOCTURNA', 'Biblioteca nocturna', 'ESTUDIO_ACADEMICO'),

-- DEPORTE & FITNESS
('BASQUETBOL', 'Básquetbol', 'DEPORTE_FITNESS'),
('TENIS', 'Tenis / Tenis de mesa', 'DEPORTE_FITNESS'),
('GIMNASIO', 'Gimnasio', 'DEPORTE_FITNESS'),
('FUTBOL_DEPORTES', 'Fútbol & Deportes', 'DEPORTE_FITNESS'),
('YOGA_MEDITACION', 'Yoga & Meditación', 'DEPORTE_FITNESS'),
('CICLISMO', 'Ciclismo', 'DEPORTE_FITNESS'),
('PARQUES_NATURALEZA', 'Parques & Naturaleza', 'DEPORTE_FITNESS'),
('ACTIVIDADES_EXTREMAS', 'Actividades extremas', 'DEPORTE_FITNESS'),

-- GASTRONOMÍA & SOCIAL
('COMER_EN_CAMPUS', 'Comer en campus', 'GASTRONOMIA_SOCIAL'),
('FOOD_TRUCKS', 'Food trucks', 'GASTRONOMIA_SOCIAL'),
('CAFETERIAS_OCULTAS', 'Cafeterías ocultas', 'GASTRONOMIA_SOCIAL'),
('COMIDA_INTERNACIONAL', 'Comida internacional', 'GASTRONOMIA_SOCIAL'),
('PICNICS_ASADOS', 'Picnics & Asados', 'GASTRONOMIA_SOCIAL'),
('INTERCAMBIO_DE_RECETAS', 'Intercambio de recetas', 'GASTRONOMIA_SOCIAL'),

-- TECNOLOGÍA & GAMING
('VIDEOJUEGOS_COMPETITIVOS', 'Videojuegos competitivos', 'TECNOLOGIA_GAMING'),
('HACKATHONES_DE_CODIGO', 'Hackathones de código', 'TECNOLOGIA_GAMING'),
('DESARROLLO_WEB_APP', 'Desarrollo web/app', 'TECNOLOGIA_GAMING'),
('IA_MACHINE_LEARNING', 'IA & Machine Learning', 'TECNOLOGIA_GAMING'),
('STREAMING_CONTENT', 'Streaming & Content', 'TECNOLOGIA_GAMING'),
('ROBOTICA', 'Robótica', 'TECNOLOGIA_GAMING'),

-- ARTE & CULTURA
('EXPOSICIONES', 'Exposiciones', 'ARTE_CULTURA'),
('CINE_PELICULAS', 'Cine & Películas', 'ARTE_CULTURA'),
('TEATRO_DANZA', 'Teatro & Danza', 'ARTE_CULTURA'),
('FOTOGRAFIA', 'Fotografía', 'ARTE_CULTURA'),
('MURALES_STREET_ART', 'Murales & Street art', 'ARTE_CULTURA'),
('LITERATURA_POESIA', 'Literatura & Poesía', 'ARTE_CULTURA'),

-- COMPETENCIAS & RETOS
('COMPETENCIAS_DEPORTIVAS', 'Competencias deportivas', 'COMPETENCIAS_RETOS'),
('CONCURSOS_ACADEMICOS', 'Concursos académicos', 'COMPETENCIAS_RETOS'),
('TORNEOS_DE_JUEGOS', 'Torneos de juegos', 'COMPETENCIAS_RETOS'),
('DESAFIOS_DE_INNOVACION', 'Desafíos de innovación', 'COMPETENCIAS_RETOS'),
('MARATONES_DE_PROGRAMACION', 'Maratones de programación', 'COMPETENCIAS_RETOS'),
('COMPETENCIAS_DE_EMPRENDIMIENTO', 'Competencias de emprendimiento', 'COMPETENCIAS_RETOS'),

-- PROFESIONAL & NETWORKING
('CHARLAS_DE_EMPRESAS', 'Charlas de empresas', 'PROFESIONAL_NETWORKING'),
('FERIAS_DE_EMPLEO', 'Ferias de empleo', 'PROFESIONAL_NETWORKING'),
('MENTORIAS', 'Mentorías', 'PROFESIONAL_NETWORKING'),
('GRUPOS_PROFESIONALES', 'Grupos profesionales', 'PROFESIONAL_NETWORKING'),
('CONFERENCIAS', 'Conferencias', 'PROFESIONAL_NETWORKING'),
('NETWORKING_EVENTS', 'Networking events', 'PROFESIONAL_NETWORKING'),

-- SOSTENIBILIDAD & CAUSA SOCIAL
('RECICLAJE_ECOLOGIA', 'Reciclaje & Ecología', 'SOSTENIBILIDAD_CAUSA_SOCIAL'),
('VOLUNTARIADO', 'Voluntariado', 'SOSTENIBILIDAD_CAUSA_SOCIAL'),
('PROYECTOS_SOCIALES', 'Proyectos sociales', 'SOSTENIBILIDAD_CAUSA_SOCIAL'),
('DERECHOS_HUMANOS', 'Derechos humanos', 'SOSTENIBILIDAD_CAUSA_SOCIAL'),
('COMUNIDAD_LGBTQ', 'Comunidad LGBTQ+', 'SOSTENIBILIDAD_CAUSA_SOCIAL'),
('INICIATIVAS_CAMPESINAS', 'Iniciativas campesinas', 'SOSTENIBILIDAD_CAUSA_SOCIAL'),

-- VIAJES & AVENTURA
('ROAD_TRIPS', 'Road trips', 'VIAJES_AVENTURA'),
('VIAJES_INTERNACIONALES', 'Viajes internacionales', 'VIAJES_AVENTURA'),
('INTERCAMBIOS_ACADEMICOS', 'Intercambios académicos', 'VIAJES_AVENTURA'),
('BACKPACKING', 'Backpacking', 'VIAJES_AVENTURA'),
('VIAJES_A_PUEBLOS', 'Viajes a pueblos', 'VIAJES_AVENTURA'),
('EXPERIENCIAS_RURALES', 'Experiencias rurales', 'VIAJES_AVENTURA'),

-- BIENESTAR & SALUD MENTAL
('MEDITACION', 'Meditación', 'BIENESTAR_SALUD_MENTAL'),
('TERAPIA_APOYO', 'Terapia & Apoyo', 'BIENESTAR_SALUD_MENTAL'),
('NUTRICION', 'Nutrición', 'BIENESTAR_SALUD_MENTAL'),
('SUENO_DESCANSO', 'Sueño & Descanso', 'BIENESTAR_SALUD_MENTAL'),
('MINDFULNESS', 'Mindfulness', 'BIENESTAR_SALUD_MENTAL'),
('COMUNIDAD_DE_BIENESTAR', 'Comunidad de bienestar', 'BIENESTAR_SALUD_MENTAL');
