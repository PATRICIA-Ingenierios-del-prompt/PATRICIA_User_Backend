package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.LogroTipo;
import com.escuelaing.usuarios.domain.port.outbound.LogroRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.LogroUsuarioEntity;
import com.escuelaing.usuarios.infrastructure.persistence.repository.LogroUsuarioJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptador de persistencia para los logros efectivamente desbloqueados.
 * Implementa el puerto de salida LogroRepositoryPort usando Spring Data JPA.
 */
@Component
public class LogroRepositoryAdapter implements LogroRepositoryPort {

    private final LogroUsuarioJpaRepository jpaRepository;

    public LogroRepositoryAdapter(LogroUsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean estaDesbloqueado(UUID usuarioId, LogroTipo tipo) {
        return jpaRepository.existsByUsuarioIdAndCodigo(usuarioId, tipo.name());
    }

    @Override
    public boolean otorgarSiNoExiste(UUID usuarioId, LogroTipo tipo, int xp) {
        if (jpaRepository.existsByUsuarioIdAndCodigo(usuarioId, tipo.name())) {
            return false;
        }
        LogroUsuarioEntity entity = LogroUsuarioEntity.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .codigo(tipo.name())
                .xpOtorgado(xp)
                .fechaDesbloqueo(Instant.now())
                .build();
        try {
            jpaRepository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            // Condición de carrera entre la evaluación lazy (GET) y la
            // oportunista (consumer): la constraint única (usuario_id,
            // codigo) ya lo dedujo, no es un error real.
            return false;
        }
    }

    @Override
    public Map<LogroTipo, Instant> buscarDesbloqueadosConFecha(UUID usuarioId) {
        Map<LogroTipo, Instant> resultado = new HashMap<>();
        for (LogroUsuarioEntity entity : jpaRepository.findByUsuarioId(usuarioId)) {
            resultado.put(LogroTipo.valueOf(entity.getCodigo()), entity.getFechaDesbloqueo());
        }
        return resultado;
    }

    @Override
    public int calcularXpTotal(UUID usuarioId) {
        return jpaRepository.sumarXpPorUsuarioId(usuarioId);
    }
}
