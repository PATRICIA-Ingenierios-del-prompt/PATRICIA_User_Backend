package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.SupportTicket;
import com.escuelaing.usuarios.domain.port.outbound.SupportRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.SupportTicketEntity;
import com.escuelaing.usuarios.infrastructure.persistence.repository.SupportTicketJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class SupportTicketRepositoryAdapter implements SupportRepositoryPort {

    private final SupportTicketJpaRepository jpaRepository;

    public SupportTicketRepositoryAdapter(SupportTicketJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SupportTicket save(SupportTicket ticket) {
        SupportTicketEntity entity = toEntity(ticket);
        SupportTicketEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<SupportTicket> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<SupportTicket> findAll() {
        return jpaRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDomain)
                .toList();
    }

    private SupportTicketEntity toEntity(SupportTicket ticket) {
        return SupportTicketEntity.builder()
                .id(ticket.getId())
                .name(ticket.getName())
                .email(ticket.getEmail())
                .message(ticket.getMessage())
                .status(ticket.getStatus())
                .createdAt(ticket.getCreatedAt())
                .resolvedAt(ticket.getResolvedAt())
                .build();
    }

    private SupportTicket toDomain(SupportTicketEntity entity) {
        return SupportTicket.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .message(entity.getMessage())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .build();
    }
}
