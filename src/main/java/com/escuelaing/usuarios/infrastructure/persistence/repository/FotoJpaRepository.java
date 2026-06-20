package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.FotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FotoJpaRepository extends JpaRepository<FotoEntity, UUID> {

    List<FotoEntity> findByUsuarioIdOrderByOrdenAsc(UUID usuarioId);

    void deleteByUsuarioIdAndId(UUID usuarioId, UUID id);
}
