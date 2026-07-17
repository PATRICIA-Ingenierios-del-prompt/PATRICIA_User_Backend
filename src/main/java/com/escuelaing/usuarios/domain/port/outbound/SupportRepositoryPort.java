package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.SupportTicket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportRepositoryPort {
    SupportTicket save(SupportTicket ticket);

    Optional<SupportTicket> findById(UUID id);

    List<SupportTicket> findAll();
}
