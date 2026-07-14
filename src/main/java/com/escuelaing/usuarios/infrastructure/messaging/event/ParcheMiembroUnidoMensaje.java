package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

/**
 * Mensaje consumido del exchange externo parche.events (RK
 * parche.member.joined). Contrato real confirmado por parche-service:
 * ParcheMemberJoinedEvent = {parcheId, memberId, category}.
 */
public record ParcheMiembroUnidoMensaje(UUID parcheId, UUID memberId, String category) {
}
