package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.Usuario;

import java.util.List;
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

    /**
     * Devuelve todos los usuarios en estado PENDING_DELETION cuya
     * {@code fechaSolicitudEliminacion} sea anterior al instante indicado.
     * Usado por el scheduler para purgar cuentas con gracia expirada.
     */
    List<Usuario> buscarPendientesDeEliminacionAnterioresA(java.time.Instant limite);

    void eliminarPorId(UUID id);
}
