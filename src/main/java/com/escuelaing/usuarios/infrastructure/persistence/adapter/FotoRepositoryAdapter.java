package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.outbound.FotoRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.FotoEntity;
import com.escuelaing.usuarios.infrastructure.persistence.mapper.FotoEntityMapper;
import com.escuelaing.usuarios.infrastructure.persistence.repository.FotoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador de persistencia para las fotos del álbum. Implementa el puerto
 * de salida FotoRepositoryPort usando Spring Data JPA.
 */
@Component
public class FotoRepositoryAdapter implements FotoRepositoryPort {

    private final FotoJpaRepository jpaRepository;
    private final FotoEntityMapper mapper;

    public FotoRepositoryAdapter(FotoJpaRepository jpaRepository, FotoEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Foto> buscarPorUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuarioIdOrderByOrdenAsc(usuarioId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Foto guardar(Foto foto) {
        var entity = mapper.toEntity(foto);
        var guardada = jpaRepository.save(entity);
        return mapper.toDomain(guardada);
    }

    @Override
    public void eliminar(UUID fotoId) {
        jpaRepository.deleteById(fotoId);
    }

    @Override
    public void guardarTodas(List<Foto> fotos) {
        List<FotoEntity> entities = fotos.stream()
                .map(mapper::toEntity)
                .collect(Collectors.toList());
        jpaRepository.saveAll(entities);
    }
}
