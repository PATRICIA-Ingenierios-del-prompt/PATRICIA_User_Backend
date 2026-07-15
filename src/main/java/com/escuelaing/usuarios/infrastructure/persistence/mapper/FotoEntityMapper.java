package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.infrastructure.persistence.entity.FotoEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el modelo de dominio Foto y la entidad de persistencia
 * FotoEntity.
 */
@Component
public class FotoEntityMapper {

    public Foto toDomain(FotoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Foto.reconstruir(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getUrlFoto(),
                entity.getOrden(),
                entity.getFechaSubida(),
                entity.isTienePersonaEnFoto()
        );
    }

    public FotoEntity toEntity(Foto foto) {
        if (foto == null) {
            return null;
        }
        return FotoEntity.builder()
                .id(foto.getId())
                .usuarioId(foto.getUsuarioId())
                .urlFoto(foto.getUrlFoto())
                .orden(foto.getOrden())
                .fechaSubida(foto.getFechaSubida())
                .tienePersonaEnFoto(foto.isTienePersonaEnFoto())
                .build();
    }
}
