package com.escuelaing.usuarios.infrastructure.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ para usuarios-service.
 *
 * Declara:
 * - El exchange propio patricia.usuarios (TopicExchange durable), donde
 *   este servicio publica sus eventos.
 * - Las colas que usuarios-service consume, junto con sus bindings hacia
 *   exchanges externos (patricia.auth, patricia.parches, patricia.sesiones)
 *   que NO se declaran aquí (se asume que ya existen, declarados por sus
 *   respectivos servicios propietarios).
 */
@Configuration
public class RabbitMqConfig {

    // --- Exchange propio ---
    public static final String EXCHANGE_USUARIOS = "patricia.usuarios";

    // --- Routing keys publicadas ---
    public static final String RK_USUARIO_CREADO = "usuario.creado";
    public static final String RK_USUARIO_ACTUALIZADO = "usuario.actualizado";
    public static final String RK_USUARIO_SUSPENDIDO = "usuario.suspendido";
    public static final String RK_USUARIO_BANEADO = "usuario.baneado";
    public static final String RK_PERFIL_ACTUALIZADO = "perfil.actualizado";
    public static final String RK_INTERESES_ACTUALIZADOS = "usuario.intereses.actualizados";
    public static final String RK_DISPONIBILIDAD_CAMBIADA = "disponibilidad.cambiada";
    public static final String RK_ALBUM_FOTO_AGREGADA = "album.foto.agregada";
    public static final String RK_ALBUM_FOTO_ELIMINADA = "album.foto.eliminada";
    public static final String RK_ALBUM_FOTO_PERSONA_DETECTADA = "album.foto.persona.detectada";
    public static final String RK_USUARIO_ELIMINADO = "usuario.eliminado";

    // --- Exchange propio nuevo para logros (separado de patricia.usuarios
    // a pedido de producto, para que Notifications se suscriba limpio) ---
    public static final String EXCHANGE_LOGROS = "patricia.logros";
    public static final String RK_LOGRO_DESBLOQUEADO = "logro.desbloqueado";

    // --- Exchanges externos (NO declarados aquí) ---
    public static final String EXCHANGE_AUTH = "patricia.auth";
    public static final String EXCHANGE_PARCHES = "patricia.parches";
    public static final String EXCHANGE_SESIONES = "patricia.sesiones";
    public static final String EXCHANGE_PARCHE_EVENTS = "parche.events";
    public static final String EXCHANGE_EVENT_EVENTS = "event.events";
    public static final String EXCHANGE_MATCHING = "patricia.matching";

    // --- Colas propias de usuarios-service ---
    public static final String QUEUE_AUTH_SESIONES = "usuarios.auth-sesiones";
    public static final String QUEUE_AUTH_FALLIDOS = "usuarios.auth-fallidos";
    public static final String QUEUE_REPORTES = "usuarios.reportes";
    public static final String QUEUE_SESIONES_CERRADAS = "usuarios.sesiones-cerradas";
    public static final String QUEUE_LOGROS_PARCHE_UNIDO = "usuarios.logros-parche-unido";
    public static final String QUEUE_LOGROS_EVENTO_CREADO = "usuarios.logros-evento-creado";
    public static final String QUEUE_LOGROS_EVENTO_PARTICIPANTE = "usuarios.logros-evento-participante";
    public static final String QUEUE_LOGROS_MATCH_CONFIRMADO = "usuarios.logros-match-confirmado";

    // --- Routing keys consumidas ---
    public static final String RK_SESION_INICIADA = "sesion.iniciada";
    public static final String RK_AUTH_FALLIDO = "auth.fallido";
    public static final String RK_REPORTE_EMITIDO = "reporte.emitido";
    public static final String RK_SESION_CERRADA = "sesion.cerrada";
    public static final String RK_PARCHE_MEMBER_JOINED = "parche.member.joined";
    public static final String RK_EVENT_CREATED = "event.created";
    public static final String RK_EVENT_PARTICIPANT_JOINED = "event.participant.joined";
    public static final String RK_MATCH_CONFIRMADO = "match.confirmado";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange exchangeUsuarios() {
        return new TopicExchange(EXCHANGE_USUARIOS, true, false);
    }

    @Bean
    public TopicExchange exchangeLogros() {
        return new TopicExchange(EXCHANGE_LOGROS, true, false);
    }

    // ---- Colas consumidas por usuarios-service ----

    @Bean
    public Queue queueAuthSesiones() {
        return QueueBuilder.durable(QUEUE_AUTH_SESIONES).build();
    }

    @Bean
    public Queue queueAuthFallidos() {
        return QueueBuilder.durable(QUEUE_AUTH_FALLIDOS).build();
    }

    @Bean
    public Queue queueReportes() {
        return QueueBuilder.durable(QUEUE_REPORTES).build();
    }

    @Bean
    public Queue queueSesionesCerradas() {
        return QueueBuilder.durable(QUEUE_SESIONES_CERRADAS).build();
    }

    @Bean
    public Queue queueLogrosParcheUnido() {
        return QueueBuilder.durable(QUEUE_LOGROS_PARCHE_UNIDO).build();
    }

    @Bean
    public Queue queueLogrosEventoCreado() {
        return QueueBuilder.durable(QUEUE_LOGROS_EVENTO_CREADO).build();
    }

    @Bean
    public Queue queueLogrosEventoParticipante() {
        return QueueBuilder.durable(QUEUE_LOGROS_EVENTO_PARTICIPANTE).build();
    }

    @Bean
    public Queue queueLogrosMatchConfirmado() {
        return QueueBuilder.durable(QUEUE_LOGROS_MATCH_CONFIRMADO).build();
    }

    // ---- Bindings hacia exchanges externos (no declarados por este servicio) ----
    //
    // IMPORTANTE: los exchanges patricia.auth, patricia.parches,
    // patricia.sesiones, parche.events, event.events y patricia.matching
    // son propiedad de otros microservicios y NO deben ser declarados aquí.
    // Se referencian únicamente por nombre al crear el binding, de modo que
    // RabbitAdmin no intente declararlos.

    @Bean
    public Declarable bindingAuthSesiones() {
        return new Binding(QUEUE_AUTH_SESIONES, Binding.DestinationType.QUEUE,
                EXCHANGE_AUTH, RK_SESION_INICIADA, null);
    }

    @Bean
    public Declarable bindingAuthFallidos() {
        return new Binding(QUEUE_AUTH_FALLIDOS, Binding.DestinationType.QUEUE,
                EXCHANGE_AUTH, RK_AUTH_FALLIDO, null);
    }

    @Bean
    public Declarable bindingReportes() {
        return new Binding(QUEUE_REPORTES, Binding.DestinationType.QUEUE,
                EXCHANGE_PARCHES, RK_REPORTE_EMITIDO, null);
    }

    @Bean
    public Declarable bindingSesionesCerradas() {
        return new Binding(QUEUE_SESIONES_CERRADAS, Binding.DestinationType.QUEUE,
                EXCHANGE_SESIONES, RK_SESION_CERRADA, null);
    }

    @Bean
    public Declarable bindingLogrosParcheUnido() {
        return new Binding(QUEUE_LOGROS_PARCHE_UNIDO, Binding.DestinationType.QUEUE,
                EXCHANGE_PARCHE_EVENTS, RK_PARCHE_MEMBER_JOINED, null);
    }

    @Bean
    public Declarable bindingLogrosEventoCreado() {
        return new Binding(QUEUE_LOGROS_EVENTO_CREADO, Binding.DestinationType.QUEUE,
                EXCHANGE_EVENT_EVENTS, RK_EVENT_CREATED, null);
    }

    @Bean
    public Declarable bindingLogrosEventoParticipante() {
        return new Binding(QUEUE_LOGROS_EVENTO_PARTICIPANTE, Binding.DestinationType.QUEUE,
                EXCHANGE_EVENT_EVENTS, RK_EVENT_PARTICIPANT_JOINED, null);
    }

    @Bean
    public Declarable bindingLogrosMatchConfirmado() {
        return new Binding(QUEUE_LOGROS_MATCH_CONFIRMADO, Binding.DestinationType.QUEUE,
                EXCHANGE_MATCHING, RK_MATCH_CONFIRMADO, null);
    }
}
