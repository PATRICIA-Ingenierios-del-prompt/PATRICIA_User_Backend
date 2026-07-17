package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.SupportTicket;
import com.escuelaing.usuarios.domain.port.in.SupportUseCase;
import com.escuelaing.usuarios.domain.port.outbound.SupportRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SupportService implements SupportUseCase {

    private final SupportRepositoryPort supportRepository;

    public SupportService(SupportRepositoryPort supportRepository) {
        this.supportRepository = supportRepository;
    }

    @Override
    public SupportTicket createTicket(String name, String email, String message) {
        SupportTicket ticket = SupportTicket.create(name, email, message);
        return supportRepository.save(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicket> listAllTickets() {
        return supportRepository.findAll();
    }

    @Override
    public SupportTicket resolveTicket(UUID id) {
        SupportTicket ticket = supportRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        ticket.resolve();
        return supportRepository.save(ticket);
    }
}
