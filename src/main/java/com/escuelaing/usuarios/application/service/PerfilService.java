package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.PerfilNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.domain.port.out.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.out.UsuarioEventPublisherPort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso PerfilUseCase.
 *
 * El perfil se cachea en Redis (cache "perfiles") para lecturas frecuentes;
 * cualquier escritura invalida la entrada correspondiente.
 */
@Service
@Transactional
public class PerfilService implements PerfilUseCase {

    private final PerfilRepositoryPort perfilRepository;
    private final UsuarioEventPublisherPort eventPublisher;

    public PerfilService(PerfilRepositoryPort perfilRepository,
                          UsuarioEventPublisherPort eventPublisher) {
        this.perfilRepository = perfilRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "perfiles", key = "#usuarioId")
    public Perfil obtenerPerfil(UUID usuarioId) {
        return perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil actualizarPerfil(UUID usuarioId, String bio, String carrera, Integer semestre,
                                    List<String> intereses, Disponibilidad disponibilidad) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        Disponibilidad disponibilidadAnterior = perfil.getDisponibilidad();
        List<String> camposModificados = perfil.actualizar(bio, carrera, semestre, intereses, disponibilidad);

        if (camposModificados.isEmpty()) {
            return perfil;
        }

        Perfil actualizado = perfilRepository.guardar(perfil);

        eventPublisher.publicarPerfilActualizado(usuarioId, camposModificados);

        if (camposModificados.contains("intereses")) {
            eventPublisher.publicarInteresesActualizados(usuarioId, actualizado.getIntereses());
        }
        if (camposModificados.contains("disponibilidad")
                && actualizado.getDisponibilidad() != disponibilidadAnterior) {
            eventPublisher.publicarDisponibilidadCambiada(usuarioId, actualizado.getDisponibilidad().name());
        }

        return actualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public Disponibilidad obtenerDisponibilidad(UUID usuarioId) {
        return perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId))
                .getDisponibilidad();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerIntereses(UUID usuarioId) {
        return perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId))
                .getIntereses();
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public List<String> actualizarIntereses(UUID usuarioId, List<String> intereses) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        boolean cambio = perfil.actualizarIntereses(intereses);
        if (!cambio) {
            return perfil.getIntereses();
        }

        Perfil actualizado = perfilRepository.guardar(perfil);

        eventPublisher.publicarPerfilActualizado(usuarioId, List.of("intereses"));
        eventPublisher.publicarInteresesActualizados(usuarioId, actualizado.getIntereses());

        return actualizado.getIntereses();
    }
}
