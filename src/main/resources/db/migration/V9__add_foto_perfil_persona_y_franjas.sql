-- Agrega tienePersonaEnFoto a la tabla perfiles
ALTER TABLE perfiles
    ADD COLUMN tiene_persona_en_foto BOOLEAN NOT NULL DEFAULT FALSE;

-- Tabla de franjas de disponibilidad horaria del perfil
CREATE TABLE perfil_disponibilidad_horaria (
    id           UUID PRIMARY KEY,
    perfil_id    UUID NOT NULL,
    dia_semana   VARCHAR(15) NOT NULL,
    hora_inicio  TIME NOT NULL,
    hora_fin     TIME NOT NULL,

    CONSTRAINT fk_franjas_perfil
        FOREIGN KEY (perfil_id) REFERENCES perfiles (id) ON DELETE CASCADE,
    CONSTRAINT ck_franjas_dia
        CHECK (dia_semana IN ('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY')),
    CONSTRAINT ck_franjas_horas
        CHECK (hora_inicio < hora_fin)
);

CREATE INDEX idx_franjas_perfil_id ON perfil_disponibilidad_horaria (perfil_id);
