package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia del agregado Usuario.
 * Implementado por un adaptador en infrastructure.persistence.
 */
public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorId(UUID id);

    Optional<Usuario> buscarPorEmail(String email);

    boolean existePorEmail(String email);
}
