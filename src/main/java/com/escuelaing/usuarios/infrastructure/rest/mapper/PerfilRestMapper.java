package com.escuelaing.usuarios.infrastructure.rest.mapper;

import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.FranjaHorariaResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PerfilRestMapper {

    public PerfilResponse toResponse(Perfil perfil) {
        if (perfil == null) return null;

        List<FranjaHorariaResponse> franjas = perfil.getFranjasDisponibilidad() == null
                ? List.of()
                : perfil.getFranjasDisponibilidad().stream()
                        .map(f -> new FranjaHorariaResponse(
                                f.getId(), f.getDiaSemana(), f.getHoraInicio(), f.getHoraFin()))
                        .toList();

        return new PerfilResponse(
                perfil.getId(),
                perfil.getUsuarioId(),
                perfil.getNombre(),
                perfil.getApellidos(),
                perfil.getBio(),
                perfil.getCarrera(),
                perfil.getSegundaCarrera(),
                perfil.getSemestre(),
                perfil.getFechaNacimiento(),
                perfil.getGenero(),
                perfil.getIntereses(),
                perfil.getDisponibilidad(),
                perfil.getUrlFotoPerfil(),
                perfil.isTienePersonaEnFoto(),
                franjas,
                perfil.isOnboardingCompleto()
        );
    }
}
