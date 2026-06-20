CREATE TABLE usuarios (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL,
    nombre          VARCHAR(255) NOT NULL,
    microsoft_id    VARCHAR(255),
    estado          VARCHAR(20)  NOT NULL,
    fecha_creacion       TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_actualizacion  TIMESTAMP WITH TIME ZONE NOT NULL,
    ultimo_acceso        TIMESTAMP WITH TIME ZONE,
    contador_reportes    INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT uq_usuarios_microsoft_id UNIQUE (microsoft_id),
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVE', 'SUSPENDED', 'BANNED')),
    CONSTRAINT ck_usuarios_email_dominio CHECK (
        email ~ '^[A-Za-z0-9._%+-]+@(mail\.escuelaing\.edu\.co|escuelaing\.edu\.co)$'
    )
);

CREATE INDEX idx_usuarios_email ON usuarios (email);
CREATE INDEX idx_usuarios_estado ON usuarios (estado);
