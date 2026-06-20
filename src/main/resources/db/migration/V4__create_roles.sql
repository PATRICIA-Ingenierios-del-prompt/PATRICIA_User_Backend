CREATE TABLE usuario_roles (
    usuario_id  UUID NOT NULL,
    rol         VARCHAR(20) NOT NULL,

    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT pk_usuario_roles PRIMARY KEY (usuario_id, rol),
    CONSTRAINT ck_usuario_roles_rol CHECK (rol IN ('STUDENT', 'PROFESSOR', 'ADMIN', 'MODERATOR'))
);

CREATE INDEX idx_usuario_roles_usuario_id ON usuario_roles (usuario_id);

-- Todo usuario existente (si los hubiera) recibe STUDENT por defecto.
-- (Tabla vacía en esta etapa; esta sentencia es defensiva para entornos
-- donde la migración se ejecute sobre datos preexistentes.)
INSERT INTO usuario_roles (usuario_id, rol)
SELECT id, 'STUDENT' FROM usuarios
ON CONFLICT DO NOTHING;
