package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.CategoriaInteres;
import com.escuelaing.usuarios.domain.model.Interes;
import com.escuelaing.usuarios.domain.port.in.CatalogoInteresesUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.CategoriaInteresesResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.InteresRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Endpoint público (permitAll, sin JWT) para consultar el catálogo cerrado
 * de intereses de PATRICIA, agrupado por categoría.
 */
@RestController
@RequestMapping("/api/v1/intereses")
@Tag(name = "Intereses", description = "Catálogo cerrado de intereses (público)")
public class InteresController {

    private final CatalogoInteresesUseCase catalogoUseCase;
    private final InteresRestMapper mapper;

    public InteresController(CatalogoInteresesUseCase catalogoUseCase, InteresRestMapper mapper) {
        this.catalogoUseCase = catalogoUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/catalogo")
    @Operation(summary = "Obtiene el catálogo de intereses agrupado por categoría")
    public ResponseEntity<List<CategoriaInteresesResponse>> obtenerCatalogo() {
        Map<CategoriaInteres, List<Interes>> agrupado = catalogoUseCase.obtenerCatalogoAgrupado();
        return ResponseEntity.ok(mapper.toResponseList(agrupado));
    }
}
