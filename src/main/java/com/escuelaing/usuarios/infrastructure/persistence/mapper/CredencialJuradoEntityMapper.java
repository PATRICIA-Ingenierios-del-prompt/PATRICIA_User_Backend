package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.CredencialJurado;
import com.escuelaing.usuarios.infrastructure.persistence.entity.CredencialJuradoEntity;
import org.springframework.stereotype.Component;

/**
 * Traduce entre el modelo de dominio CredencialJurado y su entidad de
 * persistencia. Vive en infraestructura: el dominio no conoce JPA.
 */
@Component
public class CredencialJuradoEntityMapper {

    public CredencialJurado toDomain(CredencialJuradoEntity entity) {
        if (entity == null) {
            return null;
        }
        return CredencialJurado.reconstruir(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getUsuarioId(),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion()
        );
    }

    public CredencialJuradoEntity toEntity(CredencialJurado credencial) {
        if (credencial == null) {
            return null;
        }
        return CredencialJuradoEntity.builder()
                .id(credencial.getId())
                .email(credencial.getEmail())
                .passwordHash(credencial.getPasswordHash())
                .usuarioId(credencial.getUsuarioId())
                .fechaCreacion(credencial.getFechaCreacion())
                .fechaActualizacion(credencial.getFechaActualizacion())
                .build();
    }
}
