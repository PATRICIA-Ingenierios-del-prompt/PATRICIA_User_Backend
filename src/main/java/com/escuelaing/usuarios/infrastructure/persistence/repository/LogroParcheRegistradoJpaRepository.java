package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.LogroParcheRegistradoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LogroParcheRegistradoJpaRepository extends JpaRepository<LogroParcheRegistradoEntity, UUID> {

    boolean existsByUsuarioIdAndParcheId(UUID usuarioId, UUID parcheId);

    long countByUsuarioIdAndCategoria(UUID usuarioId, String categoria);

    boolean existsByUsuarioIdAndCategoria(UUID usuarioId, String categoria);
}
