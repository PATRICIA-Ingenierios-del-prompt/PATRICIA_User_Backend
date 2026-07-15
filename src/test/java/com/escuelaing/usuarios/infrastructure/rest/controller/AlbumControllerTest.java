package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.exception.FotoNoEncontradaException;
import com.escuelaing.usuarios.domain.exception.MaxFotosException;
import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.in.AlbumUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FotoDataUrlRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.FotoResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.FotoRestMapper;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AlbumController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AlbumControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class AlbumControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private AlbumUseCase albumUseCase;
    @MockBean  private FotoRestMapper mapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public com.escuelaing.usuarios.infrastructure.security.JwtTokenParser jwtTokenParser() {
            return new com.escuelaing.usuarios.infrastructure.security.JwtTokenParser(
                    "test-secret-test-secret-test-secret-test-secret");
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String token(UUID userId) {
        SecretKey key = Keys.hmacShaKeyFor(
                "test-secret-test-secret-test-secret-test-secret".getBytes(StandardCharsets.UTF_8));
        return "Bearer " + Jwts.builder()
                .subject(userId.toString())
                .claim("roles", List.of("STUDENT"))
                .signWith(key)
                .compact();
    }

    private FotoResponse fotoResponse(UUID usuarioId, String url, int orden) {
        return new FotoResponse(UUID.randomUUID(), usuarioId, url, orden, Instant.now(), false);
    }

    // ── sin token → 403 ───────────────────────────────────────────────────────

    @Test
    void listarFotos_sinToken_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/usuarios/{id}/fotos", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    // ── listarFotos ───────────────────────────────────────────────────────────

    @Test
    void listarFotos_conToken_retornaLista200() throws Exception {
        UUID userId = UUID.randomUUID();
        Foto foto = Foto.reconstruir(UUID.randomUUID(), userId, "https://s3/1.jpg", 1, Instant.now());
        FotoResponse resp = fotoResponse(userId, "https://s3/1.jpg", 1);

        when(albumUseCase.listarFotos(userId)).thenReturn(List.of(foto));
        when(mapper.toResponseList(any())).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/usuarios/{id}/fotos", userId)
                        .header("Authorization", token(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].urlFoto").value("https://s3/1.jpg"))
                .andExpect(jsonPath("$[0].orden").value(1))
                .andExpect(jsonPath("$[0].tienePersonaEnFoto").value(false));
    }

    // ── agregarFoto multipart ─────────────────────────────────────────────────

    @Test
    void agregarFotoMultipart_conToken_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8});

        Foto foto = Foto.reconstruir(UUID.randomUUID(), userId, "https://s3/foto.jpg", 1, Instant.now());
        FotoResponse resp = fotoResponse(userId, "https://s3/foto.jpg", 1);

        when(albumUseCase.agregarFoto(eq(userId), any(byte[].class), eq("image/jpeg"))).thenReturn(foto);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(multipart("/api/v1/usuarios/{id}/fotos", userId)
                        .file(archivo)
                        .header("Authorization", token(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.urlFoto").value("https://s3/foto.jpg"));
    }

    @Test
    void agregarFotoMultipart_sinToken_retorna403() throws Exception {
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/usuarios/{id}/fotos", UUID.randomUUID())
                        .file(archivo))
                .andExpect(status().isForbidden());
    }

    @Test
    void agregarFotoMultipart_albumLleno_retorna409() throws Exception {
        UUID userId = UUID.randomUUID();
        MockMultipartFile archivo = new MockMultipartFile(
                "file", "foto.jpg", "image/jpeg", new byte[]{1});

        when(albumUseCase.agregarFoto(any(), any(byte[].class), any()))
                .thenThrow(new MaxFotosException(6));

        mockMvc.perform(multipart("/api/v1/usuarios/{id}/fotos", userId)
                        .file(archivo)
                        .header("Authorization", token(userId)))
                .andExpect(status().isConflict());
    }

    // ── agregarFoto base64 ────────────────────────────────────────────────────

    @Test
    void agregarFotoBase64_conToken_retorna201() throws Exception {
        UUID userId = UUID.randomUUID();
        FotoDataUrlRequest req = new FotoDataUrlRequest("data:image/png;base64,AAAA");

        Foto foto = Foto.reconstruir(UUID.randomUUID(), userId, "https://s3/foto.png", 2, Instant.now());
        FotoResponse resp = fotoResponse(userId, "https://s3/foto.png", 2);

        when(albumUseCase.agregarFotoDesdeDataUrl(eq(userId), eq("data:image/png;base64,AAAA")))
                .thenReturn(foto);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/usuarios/{id}/fotos/base64", userId)
                        .header("Authorization", token(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.urlFoto").value("https://s3/foto.png"))
                .andExpect(jsonPath("$.orden").value(2));
    }

    @Test
    void agregarFotoBase64_sinDataUrl_retorna400() throws Exception {
        UUID userId = UUID.randomUUID();
        FotoDataUrlRequest req = new FotoDataUrlRequest("");

        mockMvc.perform(post("/api/v1/usuarios/{id}/fotos/base64", userId)
                        .header("Authorization", token(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ── eliminarFoto ──────────────────────────────────────────────────────────

    @Test
    void eliminarFoto_conToken_retornaNoContent204() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();

        doNothing().when(albumUseCase).eliminarFoto(userId, fotoId);

        mockMvc.perform(delete("/api/v1/usuarios/{id}/fotos/{fotoId}", userId, fotoId)
                        .header("Authorization", token(userId)))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarFoto_noExiste_retorna404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();

        doThrow(new FotoNoEncontradaException(fotoId))
                .when(albumUseCase).eliminarFoto(userId, fotoId);

        mockMvc.perform(delete("/api/v1/usuarios/{id}/fotos/{fotoId}", userId, fotoId)
                        .header("Authorization", token(userId)))
                .andExpect(status().isNotFound());
    }

    // ── marcarPersonaEnFoto ───────────────────────────────────────────────────

    @Test
    void marcarPersonaEnFoto_conToken_retorna200ConAtributoTrue() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();

        Foto foto = Foto.reconstruir(fotoId, userId, "https://s3/foto.jpg", 1, Instant.now(), true);
        FotoResponse resp = new FotoResponse(fotoId, userId, "https://s3/foto.jpg", 1, Instant.now(), true);

        when(albumUseCase.marcarPersonaEnFoto(userId, fotoId)).thenReturn(foto);
        when(mapper.toResponse(any())).thenReturn(resp);

        mockMvc.perform(put("/api/v1/usuarios/{id}/fotos/{fotoId}/persona", userId, fotoId)
                        .header("Authorization", token(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tienePersonaEnFoto").value(true));
    }

    @Test
    void marcarPersonaEnFoto_fotoNoExiste_retorna404() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();

        when(albumUseCase.marcarPersonaEnFoto(userId, fotoId))
                .thenThrow(new FotoNoEncontradaException(fotoId));

        mockMvc.perform(put("/api/v1/usuarios/{id}/fotos/{fotoId}/persona", userId, fotoId)
                        .header("Authorization", token(userId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void marcarPersonaEnFoto_sinToken_retorna403() throws Exception {
        mockMvc.perform(put("/api/v1/usuarios/{id}/fotos/{fotoId}/persona",
                        UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }
}
