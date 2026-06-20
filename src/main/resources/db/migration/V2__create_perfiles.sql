CREATE TABLE perfiles (
    id                   UUID PRIMARY KEY,
    usuario_id           UUID NOT NULL,
    bio                  VARCHAR(500),
    carrera              VARCHAR(255),
    semestre             INTEGER,
    disponibilidad       VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    url_foto_perfil      VARCHAR(1000),
    fecha_actualizacion  TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_perfiles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT uq_perfiles_usuario_id UNIQUE (usuario_id),
    CONSTRAINT ck_perfiles_disponibilidad CHECK (disponibilidad IN ('DISPONIBLE', 'OCUPADO', 'NO_MOLESTAR')),
    CONSTRAINT ck_perfiles_semestre CHECK (semestre IS NULL OR semestre >= 0)
);

CREATE INDEX idx_perfiles_usuario_id ON perfiles (usuario_id);

-- Colección de intereses asociados a un perfil (List<String> en el dominio).
CREATE TABLE perfil_intereses (
    perfil_id  UUID NOT NULL,
    posicion   INTEGER NOT NULL,
    interes    VARCHAR(100) NOT NULL,

    CONSTRAINT fk_perfil_intereses_perfil FOREIGN KEY (perfil_id) REFERENCES perfiles (id) ON DELETE CASCADE,
    CONSTRAINT pk_perfil_intereses PRIMARY KEY (perfil_id, posicion)
);
