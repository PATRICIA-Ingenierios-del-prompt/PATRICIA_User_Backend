package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.LogrosUsuario;
import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.LogroResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.LogrosResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.LogroRestMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LogroController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, LogroControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class LogroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogroUseCase logroUseCase;

    @MockBean
    private LogroRestMapper mapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.escuelaing.usuarios.infrastructure.security.JwtTokenParser jwtTokenParser() {
            return new com.escuelaing.usuarios.infrastructure.security.JwtTokenParser(
                    "test-secret-test-secret-test-secret-test-secret");
        }
    }

    private String createBearerToken(UUID userId, List<String> roles) {
        SecretKey secretKey = Keys.hmacShaKeyFor(
                "test-secret-test-secret-test-secret-test-secret".getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .signWith(secretKey)
                .compact();
        return "Bearer " + token;
    }

    @Test
    void obtenerLogros_sinToken_retorna403() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/usuarios/{id}/logros", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerLogros_conTokenValido_retornaCatalogoCompleto200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        LogrosUsuario logros = new LogrosUsuario(usuarioId, 50, List.of());
        LogrosResponse response = new LogrosResponse(usuarioId, 50, List.of(
                new LogroResponse("MONA_CODER", "Mona Coder", "Únete a un parche de Tecnología", 50, true, null)
        ));

        when(logroUseCase.obtenerLogros(usuarioId)).thenReturn(logros);
        when(mapper.toResponse(logros)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/{id}/logros", usuarioId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuarioId.toString()))
                .andExpect(jsonPath("$.xpTotal").value(50))
                .andExpect(jsonPath("$.logros[0].codigo").value("MONA_CODER"))
                .andExpect(jsonPath("$.logros[0].desbloqueado").value(true));
    }
}
