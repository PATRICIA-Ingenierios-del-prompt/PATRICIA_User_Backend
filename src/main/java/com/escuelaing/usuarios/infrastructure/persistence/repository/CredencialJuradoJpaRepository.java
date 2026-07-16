package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.infrastructure.persistence.entity.CredencialJuradoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredencialJuradoJpaRepository extends JpaRepository<CredencialJuradoEntity, UUID> {

    Optional<CredencialJuradoEntity> findByEmail(String email);
}
