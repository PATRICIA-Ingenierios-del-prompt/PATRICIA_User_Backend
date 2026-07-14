-- Catálogo de reporting de los 13 logros ("monas") del álbum de PATRICIA.
-- El catálogo "fuente de verdad" para validación en runtime vive en el enum
-- de dominio com.escuelaing.usuarios.domain.model.LogroTipo; esta tabla
-- existe para permitir consultas SQL directas/reporting sobre el catálogo,
-- igual que intereses_catalogo respecto a Interes (ver V5).
CREATE TABLE logros_catalogo (
    codigo      VARCHAR(50) PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(300) NOT NULL,
    xp          INTEGER NOT NULL CHECK (xp > 0)
);

INSERT INTO logros_catalogo (codigo, nombre, descripcion, xp) VALUES
('MONA_CODER', 'Mona Coder', 'Únete a un parche de Tecnología', 50),
('MONA_DJ', 'Mona DJ', 'Crea o únete a un parche o evento de Música', 50),
('MONA_CIENTIFICA', 'Mona Científica', 'Crea o únete a un evento de Variedad', 50),
('MONA_CULTURA', 'Mona Cultura', 'Crea o únete a un parche o evento de Entretenimiento', 50),
('MONA_TRANQUILA', 'Mona Tranquila', 'Crea o únete a un parche de Variedad y completa 3 ejercicios de bienestar', 100),
('MONA_RESPIRA', 'Mona Respira', 'Completa 3 ejercicios de bienestar', 75),
('MONA_MUSICA', 'Mona Música', 'Crea o únete a un parche o evento de Música', 50),
('MONA_GAMER', 'Mona Gamer', 'Únete a un parche de Entretenimiento', 50),
('MONA_ESTUDIOSA', 'Mona Estudiosa', 'Únete a 3 parches distintos de Estudio', 100),
('MONA_ARTE', 'Mona Arte', 'Crea o únete a un parche o evento de Arte', 50),
('MONA_AIRE_LIBRE', 'Mona Aire Libre', 'Únete a un parche de Deporte', 50),
('MONA_FOODIE', 'Mona Foodie', 'Crea o únete a un parche de Variedad', 50),
('MONA_SOCIAL', 'Mona Social', 'Consigue más de 10 matches confirmados', 100);

-- Logros efectivamente desbloqueados por cada usuario. La constraint única
-- (usuario_id, codigo) modela la regla "un logro se otorga una sola vez" y
-- además sirve de deduplicación natural ante reentregas de RabbitMQ (no hay
-- dead-letter queue ni deduplicación a nivel de broker en este servicio).
-- xp_total NO se persiste como columna: se calcula on-demand con
-- SUM(xp_otorgado) (a lo sumo 13 filas por usuario).
CREATE TABLE logros_usuario (
    id                UUID PRIMARY KEY,
    usuario_id        UUID NOT NULL,
    codigo            VARCHAR(50) NOT NULL,
    xp_otorgado       INTEGER NOT NULL,
    fecha_desbloqueo  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_logros_usuario_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_logros_usuario_catalogo FOREIGN KEY (codigo) REFERENCES logros_catalogo (codigo),
    CONSTRAINT uq_logros_usuario_usuario_codigo UNIQUE (usuario_id, codigo)
);
CREATE INDEX idx_logros_usuario_usuario_id ON logros_usuario (usuario_id);

-- Registro de parches unidos/creados relevantes para reglas que necesitan
-- estado adicional más allá de "existe o no": Mona Estudiosa (3 parches
-- STUDY distintos) y Mona Tranquila (recordar el parche VARIETY hasta que
-- llegue, en un momento distinto, el conteo de ejercicios de bienestar).
-- Solo se insertan filas de categoría STUDY o VARIETY; el resto de reglas
-- de parche son de existencia simple y ya quedan cubiertas por
-- logros_usuario. UNIQUE(usuario_id, parche_id) deduplica reentregas de
-- parche.member.joined.
CREATE TABLE logros_parches_registrados (
    id              UUID PRIMARY KEY,
    usuario_id      UUID NOT NULL,
    parche_id       UUID NOT NULL,
    categoria       VARCHAR(30) NOT NULL,
    fecha_registro  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_logros_parches_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT ck_logros_parches_categoria CHECK (categoria IN ('STUDY', 'VARIETY')),
    CONSTRAINT uq_logros_parches_usuario_parche UNIQUE (usuario_id, parche_id)
);
CREATE INDEX idx_logros_parches_usuario_categoria ON logros_parches_registrados (usuario_id, categoria);

-- Registro de matches confirmados por usuario, para el conteo de Mona
-- Social (más de 10). UNIQUE(usuario_id, match_id) deduplica reentregas de
-- match.confirmado.
CREATE TABLE logros_matches_registrados (
    id              UUID PRIMARY KEY,
    usuario_id      UUID NOT NULL,
    match_id        UUID NOT NULL,
    fecha_registro  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_logros_matches_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT uq_logros_matches_usuario_match UNIQUE (usuario_id, match_id)
);
CREATE INDEX idx_logros_matches_usuario_id ON logros_matches_registrados (usuario_id);
