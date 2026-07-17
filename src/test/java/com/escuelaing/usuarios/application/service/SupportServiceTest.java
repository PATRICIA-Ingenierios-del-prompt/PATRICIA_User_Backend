package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.SupportTicket;
import com.escuelaing.usuarios.domain.model.SupportTicketStatus;
import com.escuelaing.usuarios.domain.port.outbound.SupportRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportRepositoryPort supportRepository;

    @InjectMocks
    private SupportService supportService;

    @Test
    void createTicket_savesPendingTicket() {
        ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
        when(supportRepository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        SupportTicket result = supportService.createTicket("Karol", "karol@example.com", "Necesito ayuda");

        assertThat(result.getName()).isEqualTo("Karol");
        assertThat(result.getEmail()).isEqualTo("karol@example.com");
        assertThat(result.getMessage()).isEqualTo("Necesito ayuda");
        assertThat(result.getStatus()).isEqualTo(SupportTicketStatus.PENDING);
        assertThat(result.getCreatedAt()).isNotNull();
    }

    @Test
    void listAllTickets_returnsRepositoryResult() {
        SupportTicket ticket = SupportTicket.create("Ana", "ana@example.com", "Hola");
        when(supportRepository.findAll()).thenReturn(List.of(ticket));

        List<SupportTicket> result = supportService.listAllTickets();

        assertThat(result).containsExactly(ticket);
    }

    @Test
    void resolveTicket_marksResolvedAndSaves() {
        UUID id = UUID.randomUUID();
        SupportTicket ticket = SupportTicket.create("Ana", "ana@example.com", "Hola");
        ticket.setId(id);
        when(supportRepository.findById(id)).thenReturn(Optional.of(ticket));
        when(supportRepository.save(ticket)).thenReturn(ticket);

        SupportTicket result = supportService.resolveTicket(id);

        assertThat(result.getStatus()).isEqualTo(SupportTicketStatus.RESOLVED);
        assertThat(result.getResolvedAt()).isNotNull();
        verify(supportRepository).save(ticket);
    }

    @Test
    void resolveTicket_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(supportRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supportService.resolveTicket(id))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }
}
