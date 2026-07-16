package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.FranjaHorariaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FranjaHorariaJpaRepository extends JpaRepository<FranjaHorariaEntity, UUID> {

    List<FranjaHorariaEntity> findByPerfilId(UUID perfilId);

    void deleteByPerfilId(UUID perfilId);
}
