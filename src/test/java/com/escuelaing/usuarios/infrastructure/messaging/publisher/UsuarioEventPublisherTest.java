package com.escuelaing.usuarios.infrastructure.messaging.publisher;

import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.DisponibilidadCambiadaPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void publicarDisponibilidadCambiada_envíaAlExchangeUsuariosConRoutingKeyCorrecta() {
        publisher.publicarDisponibilidadCambiada(usuarioId, "OCUPADO");

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                org.mockito.ArgumentMatchers.eq(RabbitMqConfig.RK_DISPONIBILIDAD_CAMBIADA),
                captor.capture());

        EventoEnvelope evento = captor.getValue();
        assertThat(evento.usuarioId()).isEqualTo(usuarioId);
        assertThat(evento.tipo()).isEqualTo(RabbitMqConfig.RK_DISPONIBILIDAD_CAMBIADA);
        assertThat(evento.payload()).isInstanceOf(DisponibilidadCambiadaPayload.class);
        assertThat(((DisponibilidadCambiadaPayload) evento.payload()).disponibilidad()).isEqualTo("OCUPADO");
    }

    @Test
    void publicarUsuarioSuspendido_incluyeMotivoYCantidadDeReportes() {
        publisher.publicarUsuarioSuspendido(usuarioId, MotivoSuspension.REPORTES, 5);

        ArgumentCaptor<EventoEnvelope> captor = ArgumentCaptor.forClass(EventoEnvelope.class);
        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                org.mockito.ArgumentMatchers.eq(RabbitMqConfig.RK_USUARIO_SUSPENDIDO),
                captor.capture());

        assertThat(captor.getValue().usuarioId()).isEqualTo(usuarioId);
    }

    @Test
    void publicarFotoAgregada_usaRoutingKeyAlbumFotoAgregada() {
        UUID fotoId = UUID.randomUUID();
        publisher.publicarFotoAgregada(usuarioId, fotoId, 1);

        verify(rabbitTemplate).convertAndSend(
                org.mockito.ArgumentMatchers.eq(RabbitMqConfig.EXCHANGE_USUARIOS),
                org.mockito.ArgumentMatchers.eq(RabbitMqConfig.RK_ALBUM_FOTO_AGREGADA),
                org.mockito.ArgumentMatchers.any(EventoEnvelope.class));
    }
}
