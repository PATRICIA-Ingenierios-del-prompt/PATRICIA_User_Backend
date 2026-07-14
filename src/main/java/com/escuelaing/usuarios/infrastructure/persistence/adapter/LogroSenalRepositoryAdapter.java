package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.CategoriaActividad;
import com.escuelaing.usuarios.domain.port.outbound.LogroSenalRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.LogroMatchRegistradoEntity;
import com.escuelaing.usuarios.infrastructure.persistence.entity.LogroParcheRegistradoEntity;
import com.escuelaing.usuarios.infrastructure.persistence.repository.LogroMatchRegistradoJpaRepository;
import com.escuelaing.usuarios.infrastructure.persistence.repository.LogroParcheRegistradoJpaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptador de persistencia para las señales de actividad (parches/matches
 * registrados) que alimentan las reglas de logro con conteo o memoria entre
 * eventos. Implementa el puerto de salida LogroSenalRepositoryPort.
 */
@Component
public class LogroSenalRepositoryAdapter implements LogroSenalRepositoryPort {

    private final LogroParcheRegistradoJpaRepository parcheRepository;
    private final LogroMatchRegistradoJpaRepository matchRepository;

    public LogroSenalRepositoryAdapter(LogroParcheRegistradoJpaRepository parcheRepository,
                                       LogroMatchRegistradoJpaRepository matchRepository) {
        this.parcheRepository = parcheRepository;
        this.matchRepository = matchRepository;
    }

    @Override
    public boolean registrarParche(UUID usuarioId, UUID parcheId, CategoriaActividad categoria) {
        if (parcheRepository.existsByUsuarioIdAndParcheId(usuarioId, parcheId)) {
            return false;
        }
        LogroParcheRegistradoEntity entity = LogroParcheRegistradoEntity.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .parcheId(parcheId)
                .categoria(categoria.name())
                .fechaRegistro(Instant.now())
                .build();
        try {
            parcheRepository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    public long contarParchesDistintosPorCategoria(UUID usuarioId, CategoriaActividad categoria) {
        return parcheRepository.countByUsuarioIdAndCategoria(usuarioId, categoria.name());
    }

    @Override
    public boolean existeParcheCategoria(UUID usuarioId, CategoriaActividad categoria) {
        return parcheRepository.existsByUsuarioIdAndCategoria(usuarioId, categoria.name());
    }

    @Override
    public boolean registrarMatch(UUID usuarioId, UUID matchId) {
        if (matchRepository.existsByUsuarioIdAndMatchId(usuarioId, matchId)) {
            return false;
        }
        LogroMatchRegistradoEntity entity = LogroMatchRegistradoEntity.builder()
                .id(UUID.randomUUID())
                .usuarioId(usuarioId)
                .matchId(matchId)
                .fechaRegistro(Instant.now())
                .build();
        try {
            matchRepository.save(entity);
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Override
    public long contarMatches(UUID usuarioId) {
        return matchRepository.countByUsuarioId(usuarioId);
    }
}
