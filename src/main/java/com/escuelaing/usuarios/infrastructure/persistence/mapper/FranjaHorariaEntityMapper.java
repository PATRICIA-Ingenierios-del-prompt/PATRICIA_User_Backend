package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.infrastructure.persistence.entity.FranjaHorariaEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el modelo de dominio FranjaHoraria y su entidad de
 * persistencia FranjaHorariaEntity.
 */
@Component
public class FranjaHorariaEntityMapper {

    public FranjaHoraria toDomain(FranjaHorariaEntity entity) {
        if (entity == null) return null;
        return FranjaHoraria.reconstruir(entity.getId(), entity.getPerfilId(),
                entity.getDiaSemana(), entity.getHoraInicio(), entity.getHoraFin());
    }

    public FranjaHorariaEntity toEntity(FranjaHoraria franja) {
        if (franja == null) return null;
        return FranjaHorariaEntity.builder()
                .id(franja.getId())
                .perfilId(franja.getPerfilId())
                .diaSemana(franja.getDiaSemana())
                .horaInicio(franja.getHoraInicio())
                .horaFin(franja.getHoraFin())
                .build();
    }
}
