package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.RolPlataforma;

import java.util.Set;
import java.util.UUID;

/**
 * Contrato interno consumido por auth-service. NO CAMBIAR estructura
 * (nombres y tipos de campos deben coincidir exactamente).
 */
public record UsuarioResponse(
        UUID id,
        String email,
        String nombre,
        Set<RolPlataforma> roles,
        EstadoUsuario estado
) {}
