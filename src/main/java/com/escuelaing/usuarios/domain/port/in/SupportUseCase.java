package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.SupportTicket;

import java.util.List;
import java.util.UUID;

public interface SupportUseCase {
    SupportTicket createTicket(String name, String email, String message);

    List<SupportTicket> listAllTickets();

    SupportTicket resolveTicket(UUID id);
}
