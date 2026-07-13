package com.escuelaing.usuarios.domain.model;

/**
 * Estado del ciclo de vida de un usuario dentro de la plataforma.
 */
public enum EstadoUsuario {
    ACTIVE,
    SUSPENDED,
    BANNED,
    /** Cuenta marcada para eliminación permanente. Se borrará tras 24 h. */
    PENDING_DELETION
}
