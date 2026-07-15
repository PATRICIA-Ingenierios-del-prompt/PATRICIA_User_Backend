package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.exception.PerfilNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarFranjasRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarInteresesRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarPerfilRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FotoDataUrlRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FranjaHorariaRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.PerfilRestMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PerfilController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, PerfilControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class PerfilControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private PerfilUseCase perfilUseCase;
    @MockBean  private PerfilRestMapper mapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.escuelaing.usuarios.infrastructure.security.JwtTokenParser jwtTokenParser() {
            return new com.escuelaing.usuarios.infrastructure.security.JwtTokenParser(
                    "test-secret-test-secret-test-secret-test-secret");
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String token(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-test-secret-test-secret-test-secret".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("STUDENT"))
                .signWith(key)
                .compact();
    }

    /** Construye un PerfilResponse con todos los campos (16). */
    private PerfilResponse resp(UUID usuarioId, String nombre, String bio,
                                 Disponibilidad disp, boolean tienePersona) {
        return new PerfilResponse(
                UUID.randomUUID(), usuarioId, nombre, "Apellidos", bio,
                "Sistemas", null, 5, LocalDate.of(2000, 1, 1),
                Genero.MASCULINO, List.of("Gimnasio"), disp,
                "https://s3/foto.jpg", tienePersona, List.of(), true);
    }

    // ── sin token → 403 ───────────────────────────────────────────────────────

    @Test
    void endpoints_sinToken_retornan403() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/usuarios/{id}/perfil", id)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios/{id}/disponibilidad", id)).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/usuarios/{id}/intereses", id)).andExpect(status().isForbidden());
    }

    // ── obtenerPerfil ─────────────────────────────────────────────────────────

    @Test
    void obtenerPerfil_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "Mi bio", Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.obtenerPerfil(id)).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/{id}/perfil", id).header("Authorization", token(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.tienePersonaEnFoto").value(false));
    }

    @Test
    void obtenerPerfil_noExiste_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        when(perfilUseCase.obtenerPerfil(id)).thenThrow(new PerfilNoEncontradoException(id));

        mockMvc.perform(get("/api/v1/usuarios/{id}/perfil", id).header("Authorization", token(id)))
                .andExpect(status().isNotFound());
    }

    // ── actualizarPerfil ──────────────────────────────────────────────────────

    @Test
    void actualizarPerfil_normal_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        ActualizarPerfilRequest req = new ActualizarPerfilRequest(
                null, null, "Sistemas", null, 6, null, null, null,
                List.of("Gimnasio"), false, "Bio nueva", Disponibilidad.OCUPADO);

        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "Bio nueva", Disponibilidad.OCUPADO, false);

        when(perfilUseCase.actualizarPerfil(eq(id), eq("Bio nueva"), eq("Sistemas"),
                eq(6), eq(List.of("Gimnasio")), eq(Disponibilidad.OCUPADO))).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/perfil", id)
                        .header("Authorization", token(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibilidad").value("OCUPADO"));
    }

    @Test
    void actualizarPerfil_onboarding_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        LocalDate fechaNac = LocalDate.of(2002, 5, 20);
        ActualizarPerfilRequest req = new ActualizarPerfilRequest(
                "Carlos", "Perez", "Sistemas", null, 4, fechaNac,
                Genero.MASCULINO, "foto-data", List.of("Gimnasio"), true, null, null);

        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", null, Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.completarOnboarding(eq(id), eq("Carlos"), eq("Perez"), eq("Sistemas"),
                any(), eq(4), eq(fechaNac), eq(Genero.MASCULINO), eq("foto-data"),
                eq(List.of("Gimnasio")))).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/perfil", id)
                        .header("Authorization", token(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    // ── foto de perfil ────────────────────────────────────────────────────────

    @Test
    void actualizarFotoMultipart_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        MockMultipartFile file = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8});

        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "bio", Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.actualizarFotoPerfil(eq(id), any(byte[].class), eq("image/jpeg")))
                .thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(multipart("/api/v1/usuarios/{id}/foto", id)
                        .file(file)
                        .header("Authorization", token(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlFotoPerfil").value("https://s3/foto.jpg"));
    }

    @Test
    void actualizarFotoBase64_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        FotoDataUrlRequest req = new FotoDataUrlRequest("data:image/png;base64,AAAA");

        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "bio", Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.actualizarFotoPerfilDesdeDataUrl(eq(id), eq("data:image/png;base64,AAAA")))
                .thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(post("/api/v1/usuarios/{id}/foto/base64", id)
                        .header("Authorization", token(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void marcarPersonaEnFoto_conToken_retorna200ConFlagTrue() throws Exception {
        UUID id = UUID.randomUUID();
        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "bio", Disponibilidad.DISPONIBLE, true);

        when(perfilUseCase.marcarPersonaEnFotoPerfil(id)).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/foto/persona", id)
                        .header("Authorization", token(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tienePersonaEnFoto").value(true));
    }

    // ── disponibilidad horaria ────────────────────────────────────────────────

    @Test
    void actualizarFranjasDisponibilidad_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        ActualizarFranjasRequest req = new ActualizarFranjasRequest(List.of(
                new FranjaHorariaRequest(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(10, 0)),
                new FranjaHorariaRequest(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(16, 0))
        ));

        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "bio", Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.actualizarFranjasDisponibilidad(eq(id), any())).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/disponibilidad/horaria", id)
                        .header("Authorization", token(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarFranjasDisponibilidad_listaVacia_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        ActualizarFranjasRequest req = new ActualizarFranjasRequest(List.of());

        Perfil perfil = Perfil.crearVacio(id);
        PerfilResponse response = resp(id, "Carlos", "bio", Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.actualizarFranjasDisponibilidad(eq(id), any())).thenReturn(perfil);
        when(mapper.toResponse(perfil)).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/disponibilidad/horaria", id)
                        .header("Authorization", token(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void actualizarFranjasDisponibilidad_sinToken_retorna403() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/{id}/disponibilidad/horaria", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"franjas\":[]}"))
                .andExpect(status().isForbidden());
    }

    // ── disponibilidad general ────────────────────────────────────────────────

    @Test
    void obtenerDisponibilidad_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(perfilUseCase.obtenerDisponibilidad(id)).thenReturn(Disponibilidad.OCUPADO);

        mockMvc.perform(get("/api/v1/usuarios/{id}/disponibilidad", id)
                        .header("Authorization", token(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibilidad").value("OCUPADO"));
    }

    // ── intereses ─────────────────────────────────────────────────────────────

    @Test
    void obtenerIntereses_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(perfilUseCase.obtenerIntereses(id)).thenReturn(List.of("Gimnasio", "Fotografía"));

        mockMvc.perform(get("/api/v1/usuarios/{id}/intereses", id)
                        .header("Authorization", token(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Gimnasio"));
    }

    @Test
    void actualizarIntereses_conToken_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        ActualizarInteresesRequest req = new ActualizarInteresesRequest(List.of("Gimnasio"));

        when(perfilUseCase.actualizarIntereses(eq(id), eq(List.of("Gimnasio"))))
                .thenReturn(List.of("Gimnasio"));

        mockMvc.perform(put("/api/v1/usuarios/{id}/intereses", id)
                        .header("Authorization", token(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Gimnasio"));
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
        Perfil encontrado = Perfil.crearVacio(UUID.randomUUID());
        PerfilResponse response = resp(usuarioId, "Ana", null, Disponibilidad.DISPONIBLE, false);

        when(perfilUseCase.buscarUsuarios("ana", usuarioId, 20)).thenReturn(List.of(encontrado));
        when(mapper.toResponse(encontrado)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/buscar")
                        .param("q", "ana")
                        .header("Authorization", token(usuarioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void buscarUsuarios_conLimiteCustom_loPasaAlCasoDeUso() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        when(perfilUseCase.buscarUsuarios("ana", usuarioId, 5)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/usuarios/buscar")
                        .param("q", "ana")
                        .param("limite", "5")
                        .header("Authorization", token(usuarioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
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
        PerfilResponse response = mapper.toResponse(encontrado);

        when(perfilUseCase.buscarUsuarios("ana", usuarioId, 20)).thenReturn(List.of(encontrado));
        when(mapper.toResponse(encontrado)).thenReturn(response);

        mockMvc.perform(get("/api/v1/usuarios/buscar")
                        .param("q", "ana")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
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
