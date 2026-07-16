package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.CredencialJurado;
import com.escuelaing.usuarios.domain.port.outbound.CredencialJuradoRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.CredencialJuradoEntity;
import com.escuelaing.usuarios.infrastructure.persistence.mapper.CredencialJuradoEntityMapper;
import com.escuelaing.usuarios.infrastructure.persistence.repository.CredencialJuradoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CredencialJuradoRepositoryAdapter implements CredencialJuradoRepositoryPort {

    private final CredencialJuradoJpaRepository jpaRepository;
    private final CredencialJuradoEntityMapper mapper;

    public CredencialJuradoRepositoryAdapter(CredencialJuradoJpaRepository jpaRepository,
                                              CredencialJuradoEntityMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CredencialJurado> buscarPorEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public CredencialJurado guardar(CredencialJurado credencial) {
        CredencialJuradoEntity entity = mapper.toEntity(credencial);
        CredencialJuradoEntity guardada = jpaRepository.save(entity);
        return mapper.toDomain(guardada);
    }
}
