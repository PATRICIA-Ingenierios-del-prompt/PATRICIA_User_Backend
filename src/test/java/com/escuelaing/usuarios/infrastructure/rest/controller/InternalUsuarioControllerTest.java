package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.RolPlataforma;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.mapper.UsuarioRestMapper;
import com.escuelaing.usuarios.infrastructure.security.InternalApiKeyFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que los endpoints internos (/internal/usuarios/**) rechazan
 * peticiones sin el header X-Internal-Api-Key, y las aceptan cuando el
 * header coincide con el valor configurado.
 */
@WebMvcTest(controllers = InternalUsuarioController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, InternalUsuarioControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class InternalUsuarioControllerTest {

    private static final String VALID_KEY = "test-internal-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioUseCase usuarioUseCase;

    @MockBean
    private UsuarioRestMapper usuarioRestMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.escuelaing.usuarios.infrastructure.security.JwtTokenParser jwtTokenParser() {
            return new com.escuelaing.usuarios.infrastructure.security.JwtTokenParser(
                    "test-secret-test-secret-test-secret-test-secret");
        }
    }

    @Test
    void getUsuarioPorId_sinApiKey_retorna401() throws Exception {
        mockMvc.perform(get("/internal/usuarios/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsuarioPorId_conApiKeyInvalida_retorna401() throws Exception {
        mockMvc.perform(get("/internal/usuarios/{id}", UUID.randomUUID())
                        .header(InternalApiKeyFilter.HEADER, "clave-incorrecta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUsuarioPorId_conApiKeyValida_permiteElAcceso() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);
        when(usuarioUseCase.buscarPorId(any())).thenReturn(usuario);
        when(usuarioRestMapper.toResponse(any())).thenReturn(
                new com.escuelaing.usuarios.infrastructure.rest.dto.response.UsuarioResponse(
                        id, "test@mail.escuelaing.edu.co", "Test", Set.of(RolPlataforma.STUDENT), EstadoUsuario.ACTIVE));

        mockMvc.perform(get("/internal/usuarios/{id}", id)
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void findOrCreate_sinApiKey_retorna401() throws Exception {
        String body = objectMapper.writeValueAsString(
                new com.escuelaing.usuarios.infrastructure.rest.dto.request.FindOrCreateRequest(
                        "test@mail.escuelaing.edu.co", "Test", null));

        mockMvc.perform(post("/internal/usuarios/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}
