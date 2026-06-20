package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.infrastructure.persistence.entity.FotoEntity;
import org.mapstruct.Mapper;

/**
 * Traduce entre el modelo de dominio Foto y la entidad de persistencia
 * FotoEntity.
 */
@Mapper(componentModel = "spring")
public abstract class FotoEntityMapper {

    public Foto toDomain(FotoEntity entity) {
        if (entity == null) {
            return null;
        }
        return Foto.reconstruir(
                entity.getId(),
                entity.getUsuarioId(),
                entity.getUrlFoto(),
                entity.getOrden(),
                entity.getFechaSubida()
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
                .build();
    }
}
