package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.SupportTicket;
import com.escuelaing.usuarios.domain.port.in.SupportUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.CreateSupportTicketRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.SupportTicketResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios/support")
public class SupportController {

    private final SupportUseCase supportUseCase;

    public SupportController(SupportUseCase supportUseCase) {
        this.supportUseCase = supportUseCase;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> createTicket(@Valid @RequestBody CreateSupportTicketRequest request) {
        SupportTicket ticket = supportUseCase.createTicket(request.name(), request.email(), request.message());
        return ResponseEntity.created(URI.create("/api/v1/usuarios/support/" + ticket.getId()))
                .body(SupportTicketResponse.from(ticket));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SupportTicketResponse>> listTickets() {
        List<SupportTicketResponse> tickets = supportUseCase.listAllTickets().stream()
                .map(SupportTicketResponse::from)
                .toList();
        return ResponseEntity.ok(tickets);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupportTicketResponse> resolveTicket(@PathVariable UUID id) {
        SupportTicket ticket = supportUseCase.resolveTicket(id);
        return ResponseEntity.ok(SupportTicketResponse.from(ticket));
    }
}
