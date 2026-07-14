package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.LogroUsuarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LogroUsuarioJpaRepository extends JpaRepository<LogroUsuarioEntity, UUID> {

    boolean existsByUsuarioIdAndCodigo(UUID usuarioId, String codigo);

    List<LogroUsuarioEntity> findByUsuarioId(UUID usuarioId);

    @Query("SELECT COALESCE(SUM(l.xpOtorgado), 0) FROM LogroUsuarioEntity l WHERE l.usuarioId = :usuarioId")
    int sumarXpPorUsuarioId(@Param("usuarioId") UUID usuarioId);
}
