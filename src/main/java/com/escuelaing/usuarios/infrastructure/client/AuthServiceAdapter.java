package com.escuelaing.usuarios.infrastructure.client;

import com.escuelaing.usuarios.domain.port.outbound.AuthServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Adaptador que implementa AuthServicePort usando el cliente Feign
 * AuthInternalClient para invalidar sesiones en auth-service.
 */
@Component
public class AuthServiceAdapter implements AuthServicePort {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceAdapter.class);

    private final AuthInternalClient authInternalClient;
    private final String internalApiKey;

    public AuthServiceAdapter(AuthInternalClient authInternalClient,
                               @Value("${security.internal-api-key}") String internalApiKey) {
        this.authInternalClient = authInternalClient;
        this.internalApiKey = internalApiKey;
    }

    @Override
    public void cerrarSesion(UUID usuarioId) {
        try {
            authInternalClient.cerrarSesion(usuarioId, internalApiKey);
        } catch (Exception ex) {
            // No se debe interrumpir el flujo de suspensión por un fallo
            // transitorio de auth-service; se registra para seguimiento.
            log.error("Error al invocar cierre de sesión en auth-service para usuario {}", usuarioId, ex);
        }
    }
}
