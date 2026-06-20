package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerfilJpaRepository extends JpaRepository<PerfilEntity, UUID> {

    Optional<PerfilEntity> findByUsuarioId(UUID usuarioId);
}
