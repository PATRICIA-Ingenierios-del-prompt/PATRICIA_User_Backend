package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.CategoriaInteres;
import com.escuelaing.usuarios.domain.model.Interes;
import com.escuelaing.usuarios.domain.port.in.CatalogoInteresesUseCase;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso CatalogoInteresesUseCase. El catálogo es
 * estático (definido como enum de dominio), por lo que no requiere acceso
 * a infraestructura.
 */
@Service
public class CatalogoInteresesService implements CatalogoInteresesUseCase {

    @Override
    public Map<CategoriaInteres, List<Interes>> obtenerCatalogoAgrupado() {
        Map<CategoriaInteres, List<Interes>> agrupado = new LinkedHashMap<>();
        for (CategoriaInteres categoria : CategoriaInteres.values()) {
            List<Interes> intereses = Arrays.stream(Interes.values())
                    .filter(i -> i.getCategoria() == categoria)
                    .collect(Collectors.toList());
            agrupado.put(categoria, intereses);
        }
        return agrupado;
    }
}
