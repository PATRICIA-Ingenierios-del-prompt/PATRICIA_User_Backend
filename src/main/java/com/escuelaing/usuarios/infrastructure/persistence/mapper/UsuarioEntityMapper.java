package com.escuelaing.usuarios.infrastructure.persistence.mapper;

import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.infrastructure.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;

/**
 * Traduce entre el modelo de dominio Usuario y la entidad de persistencia
 * UsuarioEntity. Vive en infraestructura: el dominio no conoce JPA.
 */
@Mapper(componentModel = "spring")
public abstract class UsuarioEntityMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        if (entity == null) {
            return null;
        }
        return Usuario.reconstruir(
                entity.getId(),
                entity.getEmail(),
                entity.getNombre(),
                entity.getMicrosoftId(),
                entity.getEstado(),
                entity.getRoles(),
                entity.getFechaCreacion(),
                entity.getFechaActualizacion(),
                entity.getUltimoAcceso(),
                entity.getContadorReportes(),
                entity.getFechaSolicitudEliminacion()
        );
    }

    public UsuarioEntity toEntity(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        return UsuarioEntity.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombre(usuario.getNombre())
                .microsoftId(usuario.getMicrosoftId())
                .estado(usuario.getEstado())
                .roles(new java.util.HashSet<>(usuario.getRoles()))
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaActualizacion(usuario.getFechaActualizacion())
                .ultimoAcceso(usuario.getUltimoAcceso())
                .contadorReportes(usuario.getContadorReportes())
                .fechaSolicitudEliminacion(usuario.getFechaSolicitudEliminacion())
                .build();
    }
}
