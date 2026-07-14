package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import com.escuelaing.usuarios.infrastructure.persistence.mapper.PerfilEntityMapper;
import com.escuelaing.usuarios.infrastructure.persistence.repository.PerfilJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para el agregado Perfil. Implementa el puerto
 * de salida PerfilRepositoryPort usando Spring Data JPA.
 */
@Component
public class PerfilRepositoryAdapter implements PerfilRepositoryPort {

    private final PerfilJpaRepository jpaRepository;
    private final PerfilEntityMapper mapper;

    public PerfilRepositoryAdapter(PerfilJpaRepository jpaRepository, PerfilEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Perfil guardar(Perfil perfil) {
        PerfilEntity entity = mapper.toEntity(perfil);
        PerfilEntity guardada = jpaRepository.save(entity);
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<Perfil> buscarPorUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).map(mapper::toDomain);
    }

    @Override
    public List<Perfil> buscarCandidatos(UUID excluirUsuarioId, int limite) {
        return jpaRepository.buscarCandidatos(excluirUsuarioId, PageRequest.of(0, limite))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Perfil> buscarPorNombreOCarrera(String query, UUID excluirUsuarioId, int limite) {
        return jpaRepository.buscarPorNombreOCarrera(query, excluirUsuarioId, PageRequest.of(0, limite))
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
