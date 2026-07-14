package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PerfilJpaRepository extends JpaRepository<PerfilEntity, UUID> {

    Optional<PerfilEntity> findByUsuarioId(UUID usuarioId);

    /**
     * Candidatos elegibles para matching: join implícito con `usuarios`
     * (estado = ACTIVE) + onboarding completo, excluyendo un usuario dado.
     * El límite de resultados se controla con el Pageable recibido
     * (ver PerfilRepositoryAdapter#buscarCandidatos).
     */
    @Query("""
            SELECT p FROM PerfilEntity p, UsuarioEntity u
            WHERE u.id = p.usuarioId
              AND u.estado = com.escuelaing.usuarios.domain.model.EstadoUsuario.ACTIVE
              AND p.onboardingCompleto = true
              AND p.usuarioId <> :excluirUsuarioId
            ORDER BY p.fechaActualizacion DESC
            """)
    List<PerfilEntity> buscarCandidatos(@Param("excluirUsuarioId") UUID excluirUsuarioId, Pageable pageable);

    @Query("""
            SELECT p FROM PerfilEntity p, UsuarioEntity u
            WHERE u.id = p.usuarioId
              AND u.estado = com.escuelaing.usuarios.domain.model.EstadoUsuario.ACTIVE
              AND p.onboardingCompleto = true
              AND p.usuarioId <> :excluirUsuarioId
              AND (
                    LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(p.apellidos) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(p.carrera) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY p.nombre ASC
            """)
    List<PerfilEntity> buscarPorNombreOCarrera(@Param("query") String query,
                                                @Param("excluirUsuarioId") UUID excluirUsuarioId,
                                                Pageable pageable);
}
