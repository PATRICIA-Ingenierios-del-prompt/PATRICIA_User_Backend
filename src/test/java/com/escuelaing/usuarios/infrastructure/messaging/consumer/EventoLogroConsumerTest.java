package com.escuelaing.usuarios.infrastructure.messaging.consumer;

import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoCreadoMensaje;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoParticipanteUnidoMensaje;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Fija el contrato de los mensajes de event.events consumidos por el motor
 * de logros. Los JSON de este test son los que publica realmente
 * event-service (EventCreatedEvent y ParticipantJoinedEvent): si alguien
 * renombra los componentes de los records a nombres "propios" del dominio de
 * este repo, la deserialización devuelve null en silencio, CategoriaActividad
 * .fromExterno(null) no dispara ninguna regla y las monas de evento (Mona
 * Científica, y la mitad "evento" de DJ/Música/Cultura/Arte) dejan de
 * desbloquearse sin ningún error visible. Este test falla en ese caso.
 */
@ExtendWith(MockitoExtension.class)
class EventoLogroConsumerTest {

    private final Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

    @Mock
    private LogroUseCase logroUseCase;

    @InjectMocks
    private EventoLogroConsumer consumer;

    private <T> T deserializar(String json, Class<T> tipo) {
        MessageProperties props = new MessageProperties();
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setInferredArgumentType(tipo);
        Object resultado = converter.fromMessage(
                new Message(json.getBytes(StandardCharsets.UTF_8), props));
        return tipo.cast(resultado);
    }

    @Test
    void deserializaEventoCreadoConElContratoRealDeEventService() {
        UUID eventId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        String json = """
                {
                  "sourceEventId": "%s",
                  "eventId": "%s",
                  "name": "Noche de rock",
                  "ownerId": "%s",
                  "linkedToParche": false,
                  "category": "MUSIC"
                }
                """.formatted(UUID.randomUUID(), eventId, ownerId);

        EventoCreadoMensaje mensaje = deserializar(json, EventoCreadoMensaje.class);

        assertThat(mensaje.eventId()).isEqualTo(eventId);
        assertThat(mensaje.ownerId()).isEqualTo(ownerId);
        assertThat(mensaje.category()).isEqualTo("MUSIC");
    }

    @Test
    void deserializaParticipanteUnidoConElContratoRealDeEventService() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String json = """
                {
                  "eventId": "%s",
                  "userId": "%s",
                  "category": "VARIETY",
                  "joinedAt": "2026-07-17T10:15:30"
                }
                """.formatted(eventId, userId);

        EventoParticipanteUnidoMensaje mensaje =
                deserializar(json, EventoParticipanteUnidoMensaje.class);

        assertThat(mensaje.eventId()).isEqualTo(eventId);
        assertThat(mensaje.userId()).isEqualTo(userId);
        assertThat(mensaje.category()).isEqualTo("VARIETY");
    }

    @Test
    void eventoCreadoAcreditaElLogroAlOwner() {
        UUID eventId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        consumer.onEventoCreado(new EventoCreadoMensaje(eventId, ownerId, "MUSIC"));

        verify(logroUseCase).procesarActividadEvento(ownerId, eventId, "MUSIC");
    }

    @Test
    void participanteUnidoAcreditaElLogroAlParticipante() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        consumer.onEventoParticipanteUnido(
                new EventoParticipanteUnidoMensaje(eventId, userId, "VARIETY"));

        verify(logroUseCase).procesarActividadEvento(userId, eventId, "VARIETY");
    }
}
