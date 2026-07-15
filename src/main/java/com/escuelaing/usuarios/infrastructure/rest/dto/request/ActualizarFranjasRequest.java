package com.escuelaing.usuarios.infrastructure.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Body para PUT /{id}/disponibilidad/horaria.
 * Reemplaza completamente las franjas horarias del usuario.
 * Lista vacía = sin franjas declaradas.
 */
public record ActualizarFranjasRequest(
        @NotNull @Size(max = 20) @Valid List<FranjaHorariaRequest> franjas
) {}
