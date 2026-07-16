package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduce entre el modelo de dominio Perfil y la entidad de persistencia
 * PerfilEntity.
 *
 * Las franjas de disponibilidad horaria NO viven en PerfilEntity (ver
 * FranjaHorariaJpaRepository / FranjaHorariaEntityMapper): se persisten en
 * su propia tabla, gestionada aparte por PerfilRepositoryAdapter, para
 * evitar el problema de Hibernate con @OneToMany + orphanRemoval sobre una
 * FK NOT NULL (perfil_id nunca puede quedar en null intermedio).
 */
@Component
public class PerfilEntityMapper {

    public Perfil toDomain(PerfilEntity entity, List<FranjaHoraria> franjas) {
        if (entity == null) return null;

        return Perfil.reconstruir(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getNombre(),
                entity.getApellidos(),
                entity.getBio(),
                entity.getCarrera(),
                entity.getSegundaCarrera(),
                entity.getSemestre(),
                entity.getFechaNacimiento(),
                entity.getGenero(),
                entity.getIntereses(),
                entity.getDisponibilidad(),
                entity.getUrlFotoPerfil(),
                entity.isTienePersonaEnFoto(),
                franjas == null ? new ArrayList<>() : franjas,
                entity.isOnboardingCompleto(),
                entity.getFechaActualizacion()
        );
    }

    public PerfilEntity toEntity(Perfil perfil) {
        if (perfil == null) return null;

        return PerfilEntity.builder()
                .id(perfil.getId())
                .usuarioId(perfil.getUsuarioId())
                .nombre(perfil.getNombre())
                .apellidos(perfil.getApellidos())
                .bio(perfil.getBio())
                .carrera(perfil.getCarrera())
                .segundaCarrera(perfil.getSegundaCarrera())
                .semestre(perfil.getSemestre())
                .fechaNacimiento(perfil.getFechaNacimiento())
                .genero(perfil.getGenero())
                .intereses(new ArrayList<>(perfil.getIntereses()))
                .disponibilidad(perfil.getDisponibilidad())
                .urlFotoPerfil(perfil.getUrlFotoPerfil())
                .tienePersonaEnFoto(perfil.isTienePersonaEnFoto())
                .onboardingCompleto(perfil.isOnboardingCompleto())
                .fechaActualizacion(perfil.getFechaActualizacion())
                .build();
    }
}
