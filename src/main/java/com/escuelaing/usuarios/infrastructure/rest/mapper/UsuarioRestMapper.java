package com.escuelaing.usuarios.infrastructure.rest.mapper;

import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.UsuarioResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioRestMapper {

    UsuarioResponse toResponse(Usuario usuario);
}
