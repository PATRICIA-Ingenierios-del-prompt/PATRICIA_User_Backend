-- Agrega soporte para cierre de cuenta con período de gracia de 24 h.

-- 1. Nueva columna para registrar cuándo se solicitó la eliminación.
ALTER TABLE usuarios
    ADD COLUMN fecha_solicitud_eliminacion TIMESTAMP WITH TIME ZONE;

-- 2. Ampliar el CHECK de estado para incluir PENDING_DELETION.
ALTER TABLE usuarios
    DROP CONSTRAINT ck_usuarios_estado;

ALTER TABLE usuarios
    ADD CONSTRAINT ck_usuarios_estado
        CHECK (estado IN ('ACTIVE', 'SUSPENDED', 'BANNED', 'PENDING_DELETION'));

-- 3. Índice parcial para que el scheduler localice rápido las cuentas expiradas.
CREATE INDEX idx_usuarios_pending_deletion
    ON usuarios (fecha_solicitud_eliminacion)
    WHERE estado = 'PENDING_DELETION';
