package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.LogroMatchRegistradoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogroMatchRegistradoJpaRepository extends JpaRepository<LogroMatchRegistradoEntity, UUID> {

    boolean existsByUsuarioIdAndMatchId(UUID usuarioId, UUID matchId);

    long countByUsuarioId(UUID usuarioId);
}
