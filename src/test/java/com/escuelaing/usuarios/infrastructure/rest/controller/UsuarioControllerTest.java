package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.exception.EstadoUsuarioInvalidoException;
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
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UsuarioController.class)
@Import({SecurityConfig.class, MethodSecurityConfig.class, GlobalExceptionHandler.class,
        UsuarioControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class UsuarioControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private UsuarioUseCase usuarioUseCase;
    @MockBean  private UsuarioRestMapper mapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.escuelaing.usuarios.infrastructure.security.JwtTokenParser jwtTokenParser() {
            return new com.escuelaing.usuarios.infrastructure.security.JwtTokenParser(
                    "test-secret-test-secret-test-secret-test-secret");
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String token(UUID userId, List<String> roles) {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-test-secret-test-secret-test-secret".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(userId.toString())
                .claim("roles", roles)
                .signWith(key)
                .compact();
    }

    private UsuarioResponse response(UUID id, EstadoUsuario estado, Set<RolPlataforma> roles,
                                      Instant fechaSolicitudEliminacion) {
        return new UsuarioResponse(id, "test@mail.escuelaing.edu.co", "Carlos",
                roles, estado, fechaSolicitudEliminacion);
    }

    // ── actualizarRoles ───────────────────────────────────────────────────────

    @Test
    void actualizarRoles_sinToken_retorna403() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ActualizarRolesRequest(Set.of(RolPlataforma.ADMIN)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarRoles_conRolStudent_retorna403() throws Exception {
        UUID userId = UUID.randomUUID();
        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", userId)
                        .header("Authorization", token(UUID.randomUUID(), List.of("STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ActualizarRolesRequest(Set.of(RolPlataforma.ADMIN)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarRoles_conRolAdmin_retornaOk200() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Carlos", null);
        UsuarioResponse resp = response(userId, EstadoUsuario.ACTIVE, Set.of(RolPlataforma.MODERATOR), null);

        when(usuarioUseCase.actualizarRoles(eq(userId), eq(Set.of(RolPlataforma.MODERATOR)))).thenReturn(usuario);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", userId)
                        .header("Authorization", token(UUID.randomUUID(), List.of("ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ActualizarRolesRequest(Set.of(RolPlataforma.MODERATOR)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("MODERATOR"));
    }

    @Test
    void actualizarRoles_conRolModerator_retornaOk200() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Carlos", null);
        UsuarioResponse resp = response(userId, EstadoUsuario.ACTIVE, Set.of(RolPlataforma.STUDENT), null);

        when(usuarioUseCase.actualizarRoles(eq(userId), eq(Set.of(RolPlataforma.STUDENT)))).thenReturn(usuario);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/usuarios/{id}/roles", userId)
                        .header("Authorization", token(UUID.randomUUID(), List.of("MODERATOR")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ActualizarRolesRequest(Set.of(RolPlataforma.STUDENT)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
    }

    // ── cerrarCuenta ──────────────────────────────────────────────────────────

    @Test
    void cerrarCuenta_sinToken_retorna403() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/{id}/cuenta", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cerrarCuenta_conToken_retorna200ConEstadoPendingDeletion() throws Exception {
        UUID userId = UUID.randomUUID();
        Instant ahora = Instant.now();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Carlos", null);
        UsuarioResponse resp = response(userId, EstadoUsuario.PENDING_DELETION,
                Set.of(RolPlataforma.STUDENT), ahora);

        when(usuarioUseCase.cerrarCuenta(userId)).thenReturn(usuario);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(delete("/api/v1/usuarios/{id}/cuenta", userId)
                        .header("Authorization", token(userId, List.of("STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PENDING_DELETION"))
                .andExpect(jsonPath("$.fechaSolicitudEliminacion").isNotEmpty());
    }

    @Test
    void cerrarCuenta_yaEstaPendiente_retorna409() throws Exception {
        UUID userId = UUID.randomUUID();

        when(usuarioUseCase.cerrarCuenta(userId))
                .thenThrow(new EstadoUsuarioInvalidoException("La cuenta ya está marcada para eliminación"));

        mockMvc.perform(delete("/api/v1/usuarios/{id}/cuenta", userId)
                        .header("Authorization", token(userId, List.of("STUDENT"))))
                .andExpect(status().isConflict());
    }

    // ── cancelarCierreCuenta ──────────────────────────────────────────────────

    @Test
    void cancelarCierreCuenta_sinToken_retorna403() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/{id}/cuenta/cancelar", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelarCierreCuenta_conToken_retorna200ConEstadoActive() throws Exception {
        UUID userId = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Carlos", null);
        UsuarioResponse resp = response(userId, EstadoUsuario.ACTIVE, Set.of(RolPlataforma.STUDENT), null);

        when(usuarioUseCase.cancelarCierreCuenta(userId)).thenReturn(usuario);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(delete("/api/v1/usuarios/{id}/cuenta/cancelar", userId)
                        .header("Authorization", token(userId, List.of("STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVE"))
                .andExpect(jsonPath("$.fechaSolicitudEliminacion").doesNotExist());
    }

    @Test
    void cancelarCierreCuenta_noEstaPendiente_retorna409() throws Exception {
        UUID userId = UUID.randomUUID();

        when(usuarioUseCase.cancelarCierreCuenta(userId))
                .thenThrow(new EstadoUsuarioInvalidoException("La cuenta no está marcada para eliminación"));

        mockMvc.perform(delete("/api/v1/usuarios/{id}/cuenta/cancelar", userId)
                        .header("Authorization", token(userId, List.of("STUDENT"))))
                .andExpect(status().isConflict());
    }
}
