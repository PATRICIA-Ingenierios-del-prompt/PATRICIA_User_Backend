package com.escuelaing.usuarios.infrastructure.rest.mapper;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.FotoResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FotoRestMapper {

    FotoResponse toResponse(Foto foto);

    List<FotoResponse> toResponseList(List<Foto> fotos);
}
