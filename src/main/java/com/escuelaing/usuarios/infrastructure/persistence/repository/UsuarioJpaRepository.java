package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.UsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM UsuarioEntity u
            WHERE u.estado = 'PENDING_DELETION'
              AND u.fechaSolicitudEliminacion IS NOT NULL
              AND u.fechaSolicitudEliminacion < :limite
            """)
    List<UsuarioEntity> findPendingDeletionBefore(@Param("limite") Instant limite);
}
