package com.escuelaing.usuarios.infrastructure.client;

import com.escuelaing.usuarios.domain.port.outbound.BienestarPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador que implementa BienestarPort usando el cliente Feign
 * BienestarInternalClient. Un fallo de bienestar-service (caído, lento,
 * contrato distinto) no debe romper GET /logros ni los consumers: se
 * absorbe la excepción y se devuelve Optional.empty(), para reintentar en
 * la próxima evaluación.
 */
@Component
public class BienestarServiceAdapter implements BienestarPort {

    private static final Logger log = LoggerFactory.getLogger(BienestarServiceAdapter.class);

    private final BienestarInternalClient bienestarInternalClient;

    public BienestarServiceAdapter(BienestarInternalClient bienestarInternalClient) {
        this.bienestarInternalClient = bienestarInternalClient;
    }

    @Override
    public Optional<Integer> contarEjerciciosCompletados(UUID usuarioId) {
        try {
            return Optional.of(bienestarInternalClient.contarEjercicios(usuarioId).total());
        } catch (Exception ex) {
            log.error("Error al consultar ejercicios de bienestar para usuario {}", usuarioId, ex);
            return Optional.empty();
        }
    }
}
