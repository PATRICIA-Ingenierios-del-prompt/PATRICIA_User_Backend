package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.PersonaDetectorPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Ejecuta la detección de persona en la foto de perfil fuera del hilo de la
 * petición HTTP. El sidecar de visión (DeepFace/RetinaFace) puede tardar
 * varios segundos (hasta 30s de read-timeout) en responder, sobre todo con
 * el modelo "frío"; hacerlo síncrono dentro de actualizarFotoPerfil llevó a
 * que la subida pareciera fallar ("no cumple") cuando en realidad la
 * detección todavía no había terminado, y el estado correcto solo se veía
 * al recargar la página (para entonces el guardado ya se había completado).
 */
@Service
public class PersonaVerificacionAsyncService {

    private static final Logger log = LoggerFactory.getLogger(PersonaVerificacionAsyncService.class);

    private final PersonaDetectorPort personaDetector;
    private final PerfilRepositoryPort perfilRepository;
    private final UsuarioEventPublisherPort eventPublisher;

    public PersonaVerificacionAsyncService(PersonaDetectorPort personaDetector,
                                           PerfilRepositoryPort perfilRepository,
                                           UsuarioEventPublisherPort eventPublisher) {
        this.personaDetector = personaDetector;
        this.perfilRepository = perfilRepository;
        this.eventPublisher = eventPublisher;
    }

    @Async
    @Transactional
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public void verificarPersonaEnFoto(UUID usuarioId, String url) {
        try {
            if (!personaDetector.tienPersona(url)) {
                return;
            }

            Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId).orElse(null);
            if (perfil == null || !url.equals(perfil.getUrlFotoPerfil())) {
                // La foto ya cambió de nuevo desde que se disparó esta verificación;
                // el resultado de este análisis quedó obsoleto.
                return;
            }

            if (perfil.marcarPersonaDetectadaEnFoto()) {
                perfilRepository.guardar(perfil);
                eventPublisher.publicarPersonaDetectadaEnFoto(usuarioId, perfil.getId());
            }
        } catch (Exception ex) {
            log.error("Error verificando persona en foto de perfil (usuarioId={}): {}", usuarioId, ex.getMessage());
        }
    }
}
