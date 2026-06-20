package com.escuelaing.usuarios.infrastructure.rest.mapper;

import com.escuelaing.usuarios.domain.model.CategoriaInteres;
import com.escuelaing.usuarios.domain.model.Interes;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.CategoriaInteresesResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.InteresResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper manual (no aplica MapStruct directo por la transformación de Map
 * a lista de DTOs agrupados) para el catálogo de intereses.
 */
@Component
public class InteresRestMapper {

    public List<CategoriaInteresesResponse> toResponseList(Map<CategoriaInteres, List<Interes>> agrupado) {
        return agrupado.entrySet().stream()
                .map(entry -> new CategoriaInteresesResponse(
                        entry.getKey().name(),
                        entry.getKey().getEtiqueta(),
                        entry.getValue().stream()
                                .map(i -> new InteresResponse(i.name(), i.getEtiqueta()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
