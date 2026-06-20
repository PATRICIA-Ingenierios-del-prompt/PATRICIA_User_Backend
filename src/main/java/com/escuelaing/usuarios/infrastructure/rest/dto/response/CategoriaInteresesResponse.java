package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import java.util.List;

public record CategoriaInteresesResponse(
        String categoria,
        String etiqueta,
        List<InteresResponse> intereses
) {}
