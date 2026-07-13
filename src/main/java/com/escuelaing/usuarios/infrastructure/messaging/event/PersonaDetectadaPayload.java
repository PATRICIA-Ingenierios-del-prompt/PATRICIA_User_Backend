package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Payload del evento album.foto.persona.detectada.
 * Notifica a otros microservicios que la foto indicada contiene una persona.
 */
public record PersonaDetectadaPayload(UUID fotoId) {}
