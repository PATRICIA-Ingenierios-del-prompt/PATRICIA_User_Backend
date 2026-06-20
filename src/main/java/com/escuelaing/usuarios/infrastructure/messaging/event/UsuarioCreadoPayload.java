package com.escuelaing.usuarios.infrastructure.messaging.event;

import com.escuelaing.usuarios.domain.model.OrigenUsuario;

public record UsuarioCreadoPayload(
        String email,
        String nombre,
        OrigenUsuario origen
) {}
