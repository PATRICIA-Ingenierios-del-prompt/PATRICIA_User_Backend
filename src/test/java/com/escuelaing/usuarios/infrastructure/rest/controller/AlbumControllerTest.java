package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.in.AlbumUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FotoRequest;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AlbumController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AlbumControllerTest.TestConfig.class})
@TestPropertySource(properties = {
        "security.internal-api-key=test-internal-key",
        "jwt.secret=test-secret-test-secret-test-secret-test-secret"
})
class AlbumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlbumUseCase albumUseCase;

    @MockBean
    private FotoRestMapper mapper;

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
        UUID usuarioId = UUID.randomUUID();
        mockMvc.perform(get("/api/v1/usuarios/{id}/fotos", usuarioId))
                .andExpect(status().isForbidden());

        FotoRequest request = new FotoRequest("https://fotos/1.jpg");
        mockMvc.perform(post("/api/v1/usuarios/{id}/fotos", usuarioId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarFotos_conTokenValido_retornaLista200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        Foto foto = Foto.reconstruir(UUID.randomUUID(), usuarioId, "https://fotos/1.jpg", 1, Instant.now());
        FotoResponse response = new FotoResponse(foto.getId(), usuarioId, "https://fotos/1.jpg", 1, Instant.now());

        when(albumUseCase.listarFotos(usuarioId)).thenReturn(List.of(foto));
        when(mapper.toResponseList(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/usuarios/{id}/fotos", usuarioId)
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].urlFoto").value("https://fotos/1.jpg"))
                .andExpect(jsonPath("$[0].orden").value(1));
    }

    @Test
    void agregarFoto_conTokenValido_retornaCreado201() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));
        FotoRequest request = new FotoRequest("https://fotos/new.jpg");

        Foto foto = Foto.reconstruir(UUID.randomUUID(), usuarioId, "https://fotos/new.jpg", 2, Instant.now());
        FotoResponse response = new FotoResponse(foto.getId(), usuarioId, "https://fotos/new.jpg", 2, Instant.now());

        when(albumUseCase.agregarFoto(eq(usuarioId), eq("https://fotos/new.jpg"))).thenReturn(foto);
        when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/usuarios/{id}/fotos", usuarioId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.urlFoto").value("https://fotos/new.jpg"))
                .andExpect(jsonPath("$.orden").value(2));
    }

    @Test
    void actualizarFoto_conTokenValido_retornaOk200() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));
        FotoRequest request = new FotoRequest("https://fotos/updated.jpg");

        Foto foto = Foto.reconstruir(fotoId, usuarioId, "https://fotos/updated.jpg", 1, Instant.now());
        FotoResponse response = new FotoResponse(fotoId, usuarioId, "https://fotos/updated.jpg", 1, Instant.now());

        when(albumUseCase.actualizarFoto(eq(usuarioId), eq(fotoId), eq("https://fotos/updated.jpg"))).thenReturn(foto);
        when(mapper.toResponse(any())).thenReturn(response);

        mockMvc.perform(put("/api/v1/usuarios/{id}/fotos/{fotoId}", usuarioId, fotoId)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlFoto").value("https://fotos/updated.jpg"));
    }

    @Test
    void eliminarFoto_conTokenValido_retornaNoContent24() throws Exception {
        UUID usuarioId = UUID.randomUUID();
        UUID fotoId = UUID.randomUUID();
        String token = createBearerToken(usuarioId, List.of("STUDENT"));

        doNothing().when(albumUseCase).eliminarFoto(usuarioId, fotoId);

        mockMvc.perform(delete("/api/v1/usuarios/{id}/fotos/{fotoId}", usuarioId, fotoId)
                        .header("Authorization", token))
                .andExpect(status().isNoContent());
    }
}
