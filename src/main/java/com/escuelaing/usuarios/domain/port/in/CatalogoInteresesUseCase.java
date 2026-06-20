package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.CategoriaInteres;
import com.escuelaing.usuarios.domain.model.Interes;

import java.util.List;
import java.util.Map;

/**
 * Puerto de entrada (caso de uso) para consultar el catálogo cerrado de
 * intereses, agrupado por categoría. Endpoint público (permitAll).
 */
public interface CatalogoInteresesUseCase {

    Map<CategoriaInteres, List<Interes>> obtenerCatalogoAgrupado();
}
