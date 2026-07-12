package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.CategoriaInteres;
import com.escuelaing.usuarios.domain.model.Interes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CatalogoInteresesServiceTest {

    private CatalogoInteresesService catalogoService;

    @BeforeEach
    void setUp() {
        catalogoService = new CatalogoInteresesService();
    }

    @Test
    void obtenerCatalogoAgrupado_retornaMapaConTodasLasCategorias() {
        Map<CategoriaInteres, List<Interes>> catalogo = catalogoService.obtenerCatalogoAgrupado();

        assertThat(catalogo).isNotNull();
        // Verifica que todas las categorías del enum estén presentes en el mapa
        assertThat(catalogo.keySet()).containsExactlyInAnyOrder(CategoriaInteres.values());

        // Para cada categoría, verifica que los intereses correspondan
        for (Map.Entry<CategoriaInteres, List<Interes>> entry : catalogo.entrySet()) {
            CategoriaInteres categoria = entry.getKey();
            List<Interes> intereses = entry.getValue();

            assertThat(intereses).isNotEmpty();
            for (Interes interes : intereses) {
                assertThat(interes.getCategoria()).isEqualTo(categoria);
            }
        }
    }
}
