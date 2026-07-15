package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.infrastructure.persistence.entity.FranjaHorariaEntity;
import com.escuelaing.usuarios.infrastructure.persistence.entity.PerfilEntity;
import org.mapstruct.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Traduce entre el modelo de dominio Perfil y la entidad de persistencia
 * PerfilEntity.
 */
@Mapper(componentModel = "spring")
public abstract class PerfilEntityMapper {

    public Perfil toDomain(PerfilEntity entity) {
        if (entity == null) return null;

        List<FranjaHoraria> franjas = entity.getFranjasDisponibilidad() == null
                ? new ArrayList<>()
                : entity.getFranjasDisponibilidad().stream()
                        .map(f -> FranjaHoraria.reconstruir(f.getId(), f.getPerfilId(),
                                f.getDiaSemana(), f.getHoraInicio(), f.getHoraFin()))
                        .collect(Collectors.toList());

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
                franjas,
                entity.isOnboardingCompleto(),
                entity.getFechaActualizacion()
        );
    }

    public PerfilEntity toEntity(Perfil perfil) {
        if (perfil == null) return null;

        List<FranjaHorariaEntity> franjas = perfil.getFranjasDisponibilidad() == null
                ? new ArrayList<>()
                : perfil.getFranjasDisponibilidad().stream()
                        .map(f -> FranjaHorariaEntity.builder()
                                .id(f.getId())
                                .perfilId(f.getPerfilId())
                                .diaSemana(f.getDiaSemana())
                                .horaInicio(f.getHoraInicio())
                                .horaFin(f.getHoraFin())
                                .build())
                        .collect(Collectors.toList());

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
                .franjasDisponibilidad(franjas)
                .onboardingCompleto(perfil.isOnboardingCompleto())
                .fechaActualizacion(perfil.getFechaActualizacion())
                .build();
    }
}
