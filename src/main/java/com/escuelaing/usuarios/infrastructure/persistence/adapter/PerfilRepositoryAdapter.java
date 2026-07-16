package com.escuelaing.usuarios.infrastructure.persistence.adapter;

import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.infrastructure.persistence.entity.FranjaHorariaEntity;
import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import com.escuelaing.usuarios.infrastructure.persistence.mapper.FranjaHorariaEntityMapper;
import com.escuelaing.usuarios.infrastructure.persistence.mapper.PerfilEntityMapper;
import com.escuelaing.usuarios.infrastructure.persistence.repository.FranjaHorariaJpaRepository;
import com.escuelaing.usuarios.infrastructure.persistence.repository.PerfilJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia para el agregado Perfil. Implementa el puerto
 * de salida PerfilRepositoryPort usando Spring Data JPA.
 *
 * Las franjas de disponibilidad horaria se persisten con un patrón
 * delete-then-insert explícito (no @OneToMany + orphanRemoval): ver
 * PerfilEntityMapper para el porqué.
 */
@Component
public class PerfilRepositoryAdapter implements PerfilRepositoryPort {

    private final PerfilJpaRepository jpaRepository;
    private final FranjaHorariaJpaRepository franjaJpaRepository;
    private final PerfilEntityMapper mapper;
    private final FranjaHorariaEntityMapper franjaMapper;

    public PerfilRepositoryAdapter(PerfilJpaRepository jpaRepository,
                                   FranjaHorariaJpaRepository franjaJpaRepository,
                                   PerfilEntityMapper mapper,
                                   FranjaHorariaEntityMapper franjaMapper) {
        this.jpaRepository = jpaRepository;
        this.franjaJpaRepository = franjaJpaRepository;
        this.mapper = mapper;
        this.franjaMapper = franjaMapper;
    }

    @Override
    public Perfil guardar(Perfil perfil) {
        PerfilEntity entity = mapper.toEntity(perfil);
        PerfilEntity guardada = jpaRepository.save(entity);

        // Reemplazo explícito: borra todas las franjas existentes del
        // perfil y vuelve a insertar las actuales del dominio. Evita el
        // UPDATE ... SET perfil_id = NULL que intenta Hibernate al quitar
        // un elemento de una colección @OneToMany con orphanRemoval, que
        // viola la restricción NOT NULL de perfil_id.
        franjaJpaRepository.deleteByPerfilId(guardada.getId());
        List<FranjaHorariaEntity> franjaEntities = perfil.getFranjasDisponibilidad().stream()
                .map(franjaMapper::toEntity)
                .peek(f -> f.setPerfilId(guardada.getId()))
                .toList();
        List<FranjaHoraria> franjasGuardadas = franjaJpaRepository.saveAll(franjaEntities).stream()
                .map(franjaMapper::toDomain)
                .toList();

        return mapper.toDomain(guardada, franjasGuardadas);
    }

    @Override
    public Optional<Perfil> buscarPorUsuarioId(UUID usuarioId) {
        return jpaRepository.findByUsuarioId(usuarioId).map(this::conFranjas);
    }

    @Override
    public List<Perfil> buscarCandidatos(UUID excluirUsuarioId, int limite) {
        return jpaRepository.buscarCandidatos(excluirUsuarioId, PageRequest.of(0, limite))
                .stream()
                .map(this::conFranjas)
                .toList();
    }

    @Override
    public List<Perfil> buscarPorNombreOCarrera(String query, UUID excluirUsuarioId, int limite) {
        return jpaRepository.buscarPorNombreOCarrera(query, excluirUsuarioId, PageRequest.of(0, limite))
                .stream()
                .map(this::conFranjas)
                .toList();
    }

    private Perfil conFranjas(PerfilEntity entity) {
        List<FranjaHoraria> franjas = franjaJpaRepository.findByPerfilId(entity.getId()).stream()
                .map(franjaMapper::toDomain)
                .toList();
        return mapper.toDomain(entity, franjas);
    }
}
