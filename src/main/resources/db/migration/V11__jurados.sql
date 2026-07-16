-- Soporte para jurados externos: correo institucional NO aplica (pueden
-- venir de cualquier dominio), y el acceso es solo por correo+contraseña
-- (no OTP, no Microsoft). Las credenciales se cargan manualmente en esta
-- tabla; el primer login crea el Usuario/Perfil como cualquier otro alumno
-- (mismo onboarding), y queda enlazado a la credencial vía usuario_id.

-- 1) Habilitar el rol JURADO en usuario_roles.
ALTER TABLE usuario_roles DROP CONSTRAINT ck_usuario_roles_rol;
ALTER TABLE usuario_roles ADD CONSTRAINT ck_usuario_roles_rol
    CHECK (rol IN ('STUDENT', 'PROFESSOR', 'ADMIN', 'MODERATOR', 'JURADO'));

-- 2) Los jurados usan correos externos (no @escuelaing.edu.co), así que el
-- CHECK de dominio institucional a nivel de tabla ya no puede aplicar a
-- TODAS las filas de `usuarios`. La validación de dominio para las cuentas
-- normales (estudiantes vía OTP/Microsoft) se sigue aplicando en la capa de
-- aplicación (Usuario.crearNuevo / DomainValidationService en auth-service);
-- aquí solo se retira la restricción a nivel de base de datos.
ALTER TABLE usuarios DROP CONSTRAINT ck_usuarios_email_dominio;

-- 3) Credenciales de jurado: correo + hash de contraseña (bcrypt), cargadas
-- manualmente por un administrador. usuario_id se completa en el primer
-- login exitoso (findOrCreate implícito del flujo de jurado).
CREATE TABLE credenciales_jurado (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL,
    password_hash         VARCHAR(255) NOT NULL,
    usuario_id            UUID,
    fecha_creacion        TIMESTAMP WITH TIME ZONE NOT NULL,
    fecha_actualizacion   TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_credenciales_jurado_email UNIQUE (email),
    CONSTRAINT uq_credenciales_jurado_usuario_id UNIQUE (usuario_id),
    CONSTRAINT fk_credenciales_jurado_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE SET NULL
);

CREATE INDEX idx_credenciales_jurado_email ON credenciales_jurado (email);
