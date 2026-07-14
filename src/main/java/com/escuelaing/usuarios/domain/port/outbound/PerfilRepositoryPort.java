package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.Perfil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia del agregado Perfil.
 */
public interface PerfilRepositoryPort {

    Perfil guardar(Perfil perfil);

    Optional<Perfil> buscarPorUsuarioId(UUID usuarioId);

    /**
     * Perfiles de usuarios ACTIVE con onboarding completo, excluyendo
     * {@code excluirUsuarioId}, limitados a {@code limite} resultados.
     * El estado ACTIVE se filtra a nivel de consulta (join con `usuarios`),
     * por lo que el llamador puede asumir que todo lo devuelto ya cumple
     * esa condición.
     */
    List<Perfil> buscarCandidatos(UUID excluirUsuarioId, int limite);

    /**
     * Búsqueda de usuarios por nombre, apellidos o carrera (contiene,
     * insensible a mayúsculas/acentos según collation de la BD). Solo
     * usuarios ACTIVE con onboarding completo, excluyendo al que busca.
     */
    List<Perfil> buscarPorNombreOCarrera(String query, UUID excluirUsuarioId, int limite);
}
