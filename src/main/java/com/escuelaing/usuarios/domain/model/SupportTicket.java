package com.escuelaing.usuarios.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {
    private UUID id;
    private String name;
    private String email;
    private String message;
    private SupportTicketStatus status;
    private Instant createdAt;
    private Instant resolvedAt;

    public static SupportTicket create(String name, String email, String message) {
        return SupportTicket.builder()
                .id(UUID.randomUUID())
                .name(name != null && !name.isBlank() ? name : "Anónimo")
                .email(email)
                .message(message)
                .status(SupportTicketStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public void resolve() {
        this.status = SupportTicketStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}
