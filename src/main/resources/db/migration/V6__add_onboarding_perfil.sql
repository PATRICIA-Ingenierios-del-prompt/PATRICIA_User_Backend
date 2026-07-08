ALTER TABLE perfiles
    ADD COLUMN nombre               VARCHAR(255),
    ADD COLUMN apellidos            VARCHAR(255),
    ADD COLUMN segunda_carrera      VARCHAR(255),
    ADD COLUMN fecha_nacimiento     DATE,
    ADD COLUMN genero               VARCHAR(20),
    ADD COLUMN onboarding_completo  BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE perfiles
    ADD CONSTRAINT ck_perfiles_genero CHECK (genero IS NULL OR genero IN ('MASCULINO', 'FEMENINO', 'OTRO', 'ND'));


CREATE INDEX idx_perfiles_onboarding_completo ON perfiles (onboarding_completo);
