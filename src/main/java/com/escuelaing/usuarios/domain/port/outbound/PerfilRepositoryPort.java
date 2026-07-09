package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.Perfil;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia del agregado Perfil.
 */
public interface PerfilRepositoryPort {

    Perfil guardar(Perfil perfil);

    Optional<Perfil> buscarPorUsuarioId(UUID usuarioId);
}
