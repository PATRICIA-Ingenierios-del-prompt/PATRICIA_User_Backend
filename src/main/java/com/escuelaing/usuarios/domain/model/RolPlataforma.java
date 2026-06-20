package com.escuelaing.usuarios.domain.model;

/**
 * Roles de plataforma que puede tener un usuario dentro de PATRICIA.
 * Contrato compartido con auth-service: NO modificar los nombres de las
 * constantes sin coordinar con dicho servicio.
 */
public enum RolPlataforma {
    STUDENT,
    PROFESSOR,
    ADMIN,
    MODERATOR
}
