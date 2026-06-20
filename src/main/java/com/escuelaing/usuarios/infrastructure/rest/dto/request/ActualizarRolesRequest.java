package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import com.escuelaing.usuarios.domain.model.RolPlataforma;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record ActualizarRolesRequest(
        @NotEmpty Set<RolPlataforma> roles
) {}
