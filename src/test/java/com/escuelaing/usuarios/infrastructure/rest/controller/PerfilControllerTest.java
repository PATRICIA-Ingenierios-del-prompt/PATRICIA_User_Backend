package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarInteresesRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarPerfilRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.PerfilRestMapper;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PerfilController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, PerfilControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class PerfilControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PerfilUseCase perfilUseCase;

    @MockBean
    private PerfilRestMapper mapper;

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
    void endpoints_sinToken_retornan403() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/usuarios/{id}/perfil", id))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/usuarios/{id}/disponibilidad", id))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/usuarios/{id}/intereses", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerPerfil_conTokenValido_retornaPerfil200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        Perfil perfil = Perfil.crearVacio(usuarioId);
        PerfilResponse response = new PerfilResponse(
                UUID.randomUUID(), usuarioId, "Nombre", "Apellidos", "Bio", "Carrera", null,
                5, LocalDate.of(2000, 1, 1), Genero.MASCULINO, List.of("Gimnasio"),
                Disponibilidad.DISPONIBLE, "url-foto", true
        );

        when(perfilUseCase.obtenerPerfil(usuarioId)).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/{id}/perfil", usuarioId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nombre"))
                .andExpect(jsonPath("$.bio").value("Bio"));
    }

    @Test
    void actualizarPerfil_onboardingFalso_realizaActualizacionComun200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        ActualizarPerfilRequest request = new ActualizarPerfilRequest(
                null, null, "Sistemas", null, 6, null, null, null,
                List.of("Gimnasio"), false, "Nueva Bio", Disponibilidad.OCUPADO
        );

        Perfil perfil = Perfil.crearVacio(usuarioId);
        PerfilResponse response = new PerfilResponse(
                UUID.randomUUID(), usuarioId, "Nombre", "Apellidos", "Nueva Bio", "Sistemas", null,
                6, LocalDate.of(2000, 1, 1), Genero.MASCULINO, List.of("Gimnasio"),
                Disponibilidad.OCUPADO, "url-foto", true
        );

        when(perfilUseCase.actualizarPerfil(eq(usuarioId), eq("Nueva Bio"), eq("Sistemas"), eq(6), eq(List.of("Gimnasio")), eq(Disponibilidad.OCUPADO)))
                .thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/perfil", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bio").value("Nueva Bio"))
                .andExpect(jsonPath("$.disponibilidad").value("OCUPADO"));
    }

    @Test
    void actualizarPerfil_onboardingVerdadero_completaOnboarding200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        LocalDate fechaNac = LocalDate.of(2002, 5, 20);
        ActualizarPerfilRequest request = new ActualizarPerfilRequest(
                "Carlos", "Perez", "Sistemas", "Matematicas", 4, fechaNac, Genero.MASCULINO,
                "foto-s3", List.of("Gimnasio"), true, null, null
        );

        Perfil perfil = Perfil.crearVacio(usuarioId);
        PerfilResponse response = new PerfilResponse(
                UUID.randomUUID(), usuarioId, "Carlos", "Perez", null, "Sistemas", "Matematicas",
                4, fechaNac, Genero.MASCULINO, List.of("Gimnasio"),
                Disponibilidad.DISPONIBLE, "foto-s3", true
        );

        when(perfilUseCase.completarOnboarding(
                eq(usuarioId), eq("Carlos"), eq("Perez"), eq("Sistemas"), eq("Matematicas"),
                eq(4), eq(fechaNac), eq(Genero.MASCULINO), eq("foto-s3"), eq(List.of("Gimnasio"))))
                .thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/perfil", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.segundaCarrera").value("Matematicas"));
    }

    @Test
    void obtenerDisponibilidad_conTokenValido_retornaDisponibilidad200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        when(perfilUseCase.obtenerDisponibilidad(usuarioId)).thenReturn(Disponibilidad.OCUPADO);

        mockMvc.perform(get("/api/v1/usuarios/{id}/disponibilidad", usuarioId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibilidad").value("OCUPADO"));
    }

    @Test
    void obtenerIntereses_conTokenValido_retornaLista200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        when(perfilUseCase.obtenerIntereses(usuarioId)).thenReturn(List.of("Gimnasio", "Videojuegos"));

        mockMvc.perform(get("/api/v1/usuarios/{id}/intereses", usuarioId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Gimnasio"))
                .andExpect(jsonPath("$[1]").value("Videojuegos"));
    }

    @Test
    void actualizarIntereses_conTokenValido_retornaLista200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));
        ActualizarInteresesRequest request = new ActualizarInteresesRequest(List.of("Gimnasio", "Cocina"));

        when(perfilUseCase.actualizarIntereses(eq(usuarioId), eq(List.of("Gimnasio", "Cocina"))))
                .thenReturn(List.of("Gimnasio", "Cocina"));

        mockMvc.perform(put("/api/v1/usuarios/{id}/intereses", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Gimnasio"))
                .andExpect(jsonPath("$[1]").value("Cocina"));
    }

    // ── buscar ───────────────────────────────────────────────────────────────

    @Test
    void buscarUsuarios_sinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/buscar").param("q", "ana"))
                .andExpect(status().isForbidden());
    }

    @Test
    void buscarUsuarios_conTokenValido_retornaListaMapeada200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));
        Perfil encontrado = Perfil.crearVacio(UUID.randomUUID());
        PerfilResponse response = new PerfilResponse(encontrado.getId(), encontrado.getUsuarioId(),
                "Ana", "Díaz", null, "Ingeniería de Sistemas", null, 5, null, null,
                List.of(), Disponibilidad.DISPONIBLE, null, true);

        when(perfilUseCase.buscarUsuarios("ana", usuarioId, 20)).thenReturn(List.of(encontrado));
        when(mapper.toResponse(encontrado)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/buscar")
                        .param("q", "ana")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"))
                .andExpect(jsonPath("$[0].carrera").value("Ingeniería de Sistemas"));
    }

    @Test
    void buscarUsuarios_conLimiteCustom_loPasaAlCasoDeUso() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));
        when(perfilUseCase.buscarUsuarios("ana", usuarioId, 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/usuarios/buscar")
                        .param("q", "ana")
                        .param("limite", "5")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
