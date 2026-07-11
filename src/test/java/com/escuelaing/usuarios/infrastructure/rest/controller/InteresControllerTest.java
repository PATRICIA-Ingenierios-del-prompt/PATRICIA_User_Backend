package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.CategoriaInteres;
import com.escuelaing.usuarios.domain.model.Interes;
import com.escuelaing.usuarios.domain.port.in.CatalogoInteresesUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.CategoriaInteresesResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.InteresResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.InteresRestMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InteresController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, InteresControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class InteresControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogoInteresesUseCase catalogoUseCase;

    @MockBean
    private InteresRestMapper mapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.escuelaing.usuarios.infrastructure.security.JwtTokenParser jwtTokenParser() {
            return new com.escuelaing.usuarios.infrastructure.security.JwtTokenParser(
                    "test-secret-test-secret-test-secret-test-secret");
        }
    }

    @Test
    void obtenerCatalogo_sinToken_retornaOk200_porqueEsPublico() throws Exception {
        Map<CategoriaInteres, List<Interes>> agrupado = Map.of(
                CategoriaInteres.DEPORTE_FITNESS, List.of(Interes.GIMNASIO)
        );
        CategoriaInteresesResponse response = new CategoriaInteresesResponse(
                CategoriaInteres.DEPORTE_FITNESS.name(),
                "Deporte & Fitness",
                List.of(new InteresResponse(Interes.GIMNASIO.name(), "Gimnasio"))
        );

        when(catalogoUseCase.obtenerCatalogoAgrupado()).thenReturn(agrupado);
        when(mapper.toResponseList(anyMap())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/intereses/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("DEPORTE_FITNESS"))
                .andExpect(jsonPath("$[0].intereses[0].codigo").value("GIMNASIO"));
    }
}
