package com.escuelaing.usuarios.infrastructure.messaging.publisher;

import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.DisponibilidadCambiadaPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoEnvelope;
import com.escuelaing.usuarios.infrastructure.messaging.event.PersonaDetectadaPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UsuarioEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private UsuarioEventPublisherAdapter publisher;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        publisher = new UsuarioEventPublisherAdapter(rabbitTemplate);
        usuarioId = UUID.randomUUID();
    }

    // ── disponibilidad cambiada ───────────────────────────────────────────────

    @Test
    void publicarDisponibilidadCambiada_enviaAlExchangeConRoutingKeyCorrecta() {
        publisher.publicarDisponibilidadCambiada(usuarioId, "OCUPADO");

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                eq(RabbitMqConfig.RK_DISPONIBILIDAD_CAMBIADA),
                captor.capture());

        EventoEnvelope evento = captor.getValue();
        assertThat(evento.usuarioId()).isEqualTo(usuarioId);
        assertThat(evento.tipo()).isEqualTo(RabbitMqConfig.RK_DISPONIBILIDAD_CAMBIADA);
        assertThat(evento.payload()).isInstanceOf(DisponibilidadCambiadaPayload.class);
        assertThat(((DisponibilidadCambiadaPayload) evento.payload()).disponibilidad()).isEqualTo("OCUPADO");
    }

    // ── usuario suspendido ────────────────────────────────────────────────────

    @Test
    void publicarUsuarioSuspendido_incluyeMotivoYCantidadDeReportes() {
        publisher.publicarUsuarioSuspendido(usuarioId, MotivoSuspension.REPORTES, 5);

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                eq(RabbitMqConfig.RK_USUARIO_SUSPENDIDO),
                captor.capture());

        assertThat(captor.getValue().usuarioId()).isEqualTo(usuarioId);
    }

    // ── foto agregada ─────────────────────────────────────────────────────────

    @Test
    void publicarFotoAgregada_usaRoutingKeyAlbumFotoAgregada() {
        UUID fotoId = UUID.randomUUID();
        publisher.publicarFotoAgregada(usuarioId, fotoId, 1);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                eq(RabbitMqConfig.RK_ALBUM_FOTO_AGREGADA),
                any(EventoEnvelope.class));
    }

    // ── foto eliminada ────────────────────────────────────────────────────────

    @Test
    void publicarFotoEliminada_usaRoutingKeyAlbumFotoEliminada() {
        UUID fotoId = UUID.randomUUID();
        publisher.publicarFotoEliminada(usuarioId, fotoId);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                eq(RabbitMqConfig.RK_ALBUM_FOTO_ELIMINADA),
                any(EventoEnvelope.class));
    }

    // ── persona detectada en foto (nueva) ─────────────────────────────────────

    @Test
    void publicarPersonaDetectadaEnFoto_usaRoutingKeyCorrecta() {
        UUID fotoId = UUID.randomUUID();
        publisher.publicarPersonaDetectadaEnFoto(usuarioId, fotoId);

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                eq(RabbitMqConfig.RK_ALBUM_FOTO_PERSONA_DETECTADA),
                captor.capture());

        EventoEnvelope evento = captor.getValue();
        assertThat(evento.usuarioId()).isEqualTo(usuarioId);
        assertThat(evento.tipo()).isEqualTo(RabbitMqConfig.RK_ALBUM_FOTO_PERSONA_DETECTADA);
        assertThat(evento.payload()).isInstanceOf(PersonaDetectadaPayload.class);
        assertThat(((PersonaDetectadaPayload) evento.payload()).fotoId()).isEqualTo(fotoId);
    }

    // ── usuario eliminado (nueva) ─────────────────────────────────────────────

    @Test
    void publicarUsuarioEliminado_usaRoutingKeyCorrecta() {
        publisher.publicarUsuarioEliminado(usuarioId);

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                eq(RabbitMqConfig.RK_USUARIO_ELIMINADO),
                captor.capture());

        EventoEnvelope evento = captor.getValue();
        assertThat(evento.usuarioId()).isEqualTo(usuarioId);
        assertThat(evento.tipo()).isEqualTo(RabbitMqConfig.RK_USUARIO_ELIMINADO);
        assertThat(evento.payload()).isNull();
    }

    // ── envelope tiene id único y timestamp ──────────────────────────────────

    @Test
    void cadaEventoTieneEventoIdUnico() {
        publisher.publicarFotoAgregada(usuarioId, UUID.randomUUID(), 1);
        publisher.publicarFotoAgregada(usuarioId, UUID.randomUUID(), 2);

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate, times(2)).convertAndSend(any(), any(), captor.capture());

        java.util.List<EventoEnvelope> eventos = captor.getAllValues();
        assertThat(eventos.get(0).eventoId()).isNotEqualTo(eventos.get(1).eventoId());
        assertThat(eventos.get(0).timestamp()).isNotNull();
    }
}
