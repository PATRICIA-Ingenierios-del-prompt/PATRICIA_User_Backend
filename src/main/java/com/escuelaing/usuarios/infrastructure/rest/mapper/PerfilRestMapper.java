package com.escuelaing.usuarios.infrastructure.rest.mapper;

import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PerfilRestMapper {

    PerfilResponse toResponse(Perfil perfil);
}
