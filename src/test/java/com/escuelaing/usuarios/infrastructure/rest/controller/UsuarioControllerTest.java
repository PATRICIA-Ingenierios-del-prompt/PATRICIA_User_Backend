package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.RolPlataforma;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.infrastructure.config.MethodSecurityConfig;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarRolesRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.UsuarioResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.UsuarioRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, GlobalExceptionHandler.class, UsuarioControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioUseCase usuarioUseCase;

    @MockBean
    private UsuarioRestMapper mapper;

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
    void actualizarRoles_sinToken_retorna403() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        ActualizarRolesRequest request = new ActualizarRolesRequest(Set.of(RolPlataforma.ADMIN));

        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarRoles_conRolStudent_retorna403() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(UUID.randomUUID(), List.of("STUDENT"));
        ActualizarRolesRequest request = new ActualizarRolesRequest(Set.of(RolPlataforma.ADMIN));

        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarRoles_conRolAdmin_retornaOk200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(UUID.randomUUID(), List.of("ADMIN"));
        ActualizarRolesRequest request = new ActualizarRolesRequest(Set.of(RolPlataforma.MODERATOR));

        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Carlos", null);
        UsuarioResponse response = new UsuarioResponse(
                usuarioId, "test@mail.escuelaing.edu.co", "Carlos", Set.of(RolPlataforma.MODERATOR), EstadoUsuario.ACTIVE
        );

        when(usuarioUseCase.actualizarRoles(eq(usuarioId), eq(Set.of(RolPlataforma.MODERATOR)))).thenReturn(usuario);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("MODERATOR"));
    }

    @Test
    void actualizarRoles_conRolModerator_retornaOk200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(UUID.randomUUID(), List.of("MODERATOR"));
        ActualizarRolesRequest request = new ActualizarRolesRequest(Set.of(RolPlataforma.STUDENT));

        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Carlos", null);
        UsuarioResponse response = new UsuarioResponse(
                usuarioId, "test@mail.escuelaing.edu.co", "Carlos", Set.of(RolPlataforma.STUDENT), EstadoUsuario.ACTIVE
        );

        when(usuarioUseCase.actualizarRoles(eq(usuarioId), eq(Set.of(RolPlataforma.STUDENT)))).thenReturn(usuario);
        when(mapper.toResponse(any(Usuario.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
    }
}
