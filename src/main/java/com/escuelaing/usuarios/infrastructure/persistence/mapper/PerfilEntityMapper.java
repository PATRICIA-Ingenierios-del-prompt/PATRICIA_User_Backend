package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import org.mapstruct.Mapper;

import java.util.ArrayList;

/**
 * Traduce entre el modelo de dominio Perfil y la entidad de persistencia
 * PerfilEntity.
 */
@Mapper(componentModel = "spring")
public abstract class PerfilEntityMapper {

    public Perfil toDomain(PerfilEntity entity) {
        if (entity == null) {
            return null;
        }
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
                entity.isOnboardingCompleto(),
                entity.getFechaActualizacion()
        );
    }

    public PerfilEntity toEntity(Perfil perfil) {
        if (perfil == null) {
            return null;
        }
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
                .onboardingCompleto(perfil.isOnboardingCompleto())
                .fechaActualizacion(perfil.getFechaActualizacion())
                .build();
    }
}
