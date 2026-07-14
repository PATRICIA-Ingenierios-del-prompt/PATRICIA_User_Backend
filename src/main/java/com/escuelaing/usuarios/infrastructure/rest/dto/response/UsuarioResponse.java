package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.RolPlataforma;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Contrato interno consumido por auth-service. Los campos id, email, nombre,
 * roles y estado NO se pueden renombrar (contrato con auth-service).
 * Se agrega fechaSolicitudEliminacion como campo nullable para el flujo
 * de cierre de cuenta.
 */
public record UsuarioResponse(
        UUID id,
        String email,
        String nombre,
        Set<RolPlataforma> roles,
        EstadoUsuario estado,
        Instant fechaSolicitudEliminacion
) {}
