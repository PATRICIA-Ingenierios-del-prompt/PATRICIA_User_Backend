package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.domain.model.OrigenUsuario;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de salida para la publicación de eventos de dominio relacionados
 * con usuarios. Implementado por un adaptador RabbitMQ en infrastructure.messaging.
 */
public interface UsuarioEventPublisherPort {

    void publicarUsuarioCreado(UUID usuarioId, String email, String nombre, OrigenUsuario origen);

    void publicarUsuarioActualizado(UUID usuarioId, List<String> camposModificados);

    void publicarUsuarioSuspendido(UUID usuarioId, MotivoSuspension motivo, int cantidadReportes);

    void publicarUsuarioBaneado(UUID usuarioId);

    void publicarPerfilActualizado(UUID usuarioId, List<String> camposModificados);

    void publicarInteresesActualizados(UUID usuarioId, List<String> intereses);

    void publicarDisponibilidadCambiada(UUID usuarioId, String nuevaDisponibilidad);

    void publicarFotoAgregada(UUID usuarioId, UUID fotoId, int orden);

    void publicarFotoEliminada(UUID usuarioId, UUID fotoId);

    /**
     * Evento publicado cuando se detecta una persona en una foto del álbum.
     * Routing key: album.foto.persona.detectada
     */
    void publicarPersonaDetectadaEnFoto(UUID usuarioId, UUID fotoId);

    /**
     * Evento publicado cuando se elimina permanentemente la cuenta de un usuario
     * tras expirar el período de gracia de 24 h.
     * Routing key: usuario.eliminado
     */
    void publicarUsuarioEliminado(UUID usuarioId);
}
