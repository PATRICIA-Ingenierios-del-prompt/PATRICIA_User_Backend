package com.escuelaing.usuarios.infrastructure.rest.dto.response;

import com.escuelaing.usuarios.domain.model.SupportTicket;
import com.escuelaing.usuarios.domain.model.SupportTicketStatus;

import java.time.Instant;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        String name,
        String email,
        String message,
        SupportTicketStatus status,
        Instant createdAt,
        Instant resolvedAt
) {
    public static SupportTicketResponse from(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getName(),
                ticket.getEmail(),
                ticket.getMessage(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getResolvedAt()
        );
    }
}
