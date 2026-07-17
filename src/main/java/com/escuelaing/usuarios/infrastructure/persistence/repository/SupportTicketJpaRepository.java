package com.escuelaing.usuarios.infrastructure.persistence.repository;

import com.escuelaing.usuarios.domain.model.SupportTicketStatus;
import com.escuelaing.usuarios.infrastructure.persistence.entity.SupportTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportTicketJpaRepository extends JpaRepository<SupportTicketEntity, UUID> {
    List<SupportTicketEntity> findByStatusOrderByCreatedAtDesc(SupportTicketStatus status);

    List<SupportTicketEntity> findAllByOrderByCreatedAtDesc();
}
