package com.escuelaing.usuarios.infrastructure.messaging.publisher;

import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.domain.model.OrigenUsuario;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.infrastructure.messaging.config.RabbitMqConfig;
import com.escuelaing.usuarios.infrastructure.messaging.event.AlbumFotoPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.CamposModificadosPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.DisponibilidadCambiadaPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.EventoEnvelope;
import com.escuelaing.usuarios.infrastructure.messaging.event.InteresesActualizadosPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.PersonaDetectadaPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.UsuarioCreadoPayload;
import com.escuelaing.usuarios.infrastructure.messaging.event.UsuarioSuspendidoPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adaptador de mensajería que publica los eventos de dominio de
 * usuarios-service en el exchange patricia.usuarios.
 *
 * Implementa el puerto de salida UsuarioEventPublisherPort; es la única
 * pieza de infraestructura que conoce RabbitMQ para publicación de eventos.
 */
@Component
public class UsuarioEventPublisherAdapter implements UsuarioEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(UsuarioEventPublisherAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    public UsuarioEventPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicarUsuarioCreado(UUID usuarioId, String email, String nombre, OrigenUsuario origen) {
        var payload = new UsuarioCreadoPayload(email, nombre, origen);
        publicar(RabbitMqConfig.RK_USUARIO_CREADO, usuarioId, payload);
    }

    @Override
    public void publicarUsuarioActualizado(UUID usuarioId, List<String> camposModificados) {
        var payload = new CamposModificadosPayload(camposModificados);
        publicar(RabbitMqConfig.RK_USUARIO_ACTUALIZADO, usuarioId, payload);
    }

    @Override
    public void publicarUsuarioSuspendido(UUID usuarioId, MotivoSuspension motivo, int cantidadReportes) {
        var payload = new UsuarioSuspendidoPayload(motivo, cantidadReportes);
        publicar(RabbitMqConfig.RK_USUARIO_SUSPENDIDO, usuarioId, payload);
    }

    @Override
    public void publicarUsuarioBaneado(UUID usuarioId) {
        publicar(RabbitMqConfig.RK_USUARIO_BANEADO, usuarioId, null);
    }

    @Override
    public void publicarPerfilActualizado(UUID usuarioId, List<String> camposModificados) {
        var payload = new CamposModificadosPayload(camposModificados);
        publicar(RabbitMqConfig.RK_PERFIL_ACTUALIZADO, usuarioId, payload);
    }

    @Override
    public void publicarInteresesActualizados(UUID usuarioId, List<String> intereses) {
        var payload = new InteresesActualizadosPayload(intereses);
        publicar(RabbitMqConfig.RK_INTERESES_ACTUALIZADOS, usuarioId, payload);
    }

    @Override
    public void publicarDisponibilidadCambiada(UUID usuarioId, String nuevaDisponibilidad) {
        var payload = new DisponibilidadCambiadaPayload(nuevaDisponibilidad);
        publicar(RabbitMqConfig.RK_DISPONIBILIDAD_CAMBIADA, usuarioId, payload);
    }

    @Override
    public void publicarFotoAgregada(UUID usuarioId, UUID fotoId, int orden) {
        var payload = AlbumFotoPayload.agregada(fotoId, orden);
        publicar(RabbitMqConfig.RK_ALBUM_FOTO_AGREGADA, usuarioId, payload);
    }

    @Override
    public void publicarFotoEliminada(UUID usuarioId, UUID fotoId) {
        var payload = AlbumFotoPayload.eliminada(fotoId);
        publicar(RabbitMqConfig.RK_ALBUM_FOTO_ELIMINADA, usuarioId, payload);
    }

    @Override
    public void publicarPersonaDetectadaEnFoto(UUID usuarioId, UUID fotoId) {
        var payload = new PersonaDetectadaPayload(fotoId);
        publicar(RabbitMqConfig.RK_ALBUM_FOTO_PERSONA_DETECTADA, usuarioId, payload);
    }

    @Override
    public void publicarUsuarioEliminado(UUID usuarioId) {
        publicar(RabbitMqConfig.RK_USUARIO_ELIMINADO, usuarioId, null);
    }

    private <T> void publicar(String routingKey, UUID usuarioId, T payload) {
        EventoEnvelope<T> evento = EventoEnvelope.of(usuarioId, routingKey, payload);
        log.info("Publicando evento [{}] eventoId={} usuarioId={}", routingKey, evento.eventoId(), usuarioId);
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_USUARIOS, routingKey, evento);
    }
}
