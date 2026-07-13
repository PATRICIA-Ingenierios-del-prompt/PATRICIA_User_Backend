package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.model.RolPlataforma;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.exception.PerfilNoEncontradoException;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.infrastructure.config.SecurityConfig;
import com.escuelaing.usuarios.infrastructure.rest.advice.GlobalExceptionHandler;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarEstadoRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FindOrCreateRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.UsuarioResponse;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    private PerfilUseCase perfilUseCase;

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
                new UsuarioResponse(id, "test@mail.escuelaing.edu.co", "Test", Set.of(RolPlataforma.STUDENT), EstadoUsuario.ACTIVE, null));

        mockMvc.perform(get("/internal/usuarios/{id}", id)
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void findOrCreate_sinApiKey_retorna401() throws Exception {
        String body = objectMapper.writeValueAsString(
                new FindOrCreateRequest("test@mail.escuelaing.edu.co", "Test", null));

        mockMvc.perform(post("/internal/usuarios/find-or-create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findOrCreate_conApiKeyValida_creadoVerdadero_retorna201() throws Exception {
        UUID id = UUID.randomUUID();
        FindOrCreateRequest request = new FindOrCreateRequest("test@mail.escuelaing.edu.co", "Test", "ms-123");
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", "ms-123");
        UsuarioUseCase.ResultadoFindOrCreate resultado = new UsuarioUseCase.ResultadoFindOrCreate(usuario, true);

        when(usuarioUseCase.buscarOCrear(eq("test@mail.escuelaing.edu.co"), eq("Test"), eq("ms-123"))).thenReturn(resultado);
        when(usuarioRestMapper.toResponse(usuario)).thenReturn(
                new UsuarioResponse(id, "test@mail.escuelaing.edu.co", "Test", Set.of(RolPlataforma.STUDENT), EstadoUsuario.ACTIVE, null));

        mockMvc.perform(post("/internal/usuarios/find-or-create")
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@mail.escuelaing.edu.co"));
    }

    @Test
    void findOrCreate_conApiKeyValida_creadoFalso_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        FindOrCreateRequest request = new FindOrCreateRequest("test@mail.escuelaing.edu.co", "Test", "ms-123");
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", "ms-123");
        UsuarioUseCase.ResultadoFindOrCreate resultado = new UsuarioUseCase.ResultadoFindOrCreate(usuario, false);

        when(usuarioUseCase.buscarOCrear(eq("test@mail.escuelaing.edu.co"), eq("Test"), eq("ms-123"))).thenReturn(resultado);
        when(usuarioRestMapper.toResponse(usuario)).thenReturn(
                new UsuarioResponse(id, "test@mail.escuelaing.edu.co", "Test", Set.of(RolPlataforma.STUDENT), EstadoUsuario.ACTIVE, null));

        mockMvc.perform(post("/internal/usuarios/find-or-create")
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.escuelaing.edu.co"));
    }

    @Test
    void buscarPorEmail_sinApiKey_retorna401() throws Exception {
        mockMvc.perform(get("/internal/usuarios/buscar").param("email", "test@mail.escuelaing.edu.co"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarPorEmail_conApiKeyValida_usuarioEncontrado_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);
        when(usuarioUseCase.buscarPorEmail("test@mail.escuelaing.edu.co")).thenReturn(Optional.of(usuario));
        when(usuarioRestMapper.toResponse(usuario)).thenReturn(
                new UsuarioResponse(id, "test@mail.escuelaing.edu.co", "Test", Set.of(RolPlataforma.STUDENT), EstadoUsuario.ACTIVE, null));

        mockMvc.perform(get("/internal/usuarios/buscar")
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY)
                        .param("email", "test@mail.escuelaing.edu.co"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@mail.escuelaing.edu.co"));
    }

    @Test
    void buscarPorEmail_conApiKeyValida_usuarioNoEncontrado_retorna404() throws Exception {
        when(usuarioUseCase.buscarPorEmail("test@mail.escuelaing.edu.co")).thenReturn(Optional.empty());

        mockMvc.perform(get("/internal/usuarios/buscar")
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY)
                        .param("email", "test@mail.escuelaing.edu.co"))
                .andExpect(status().isNotFound());
    }

    @Test
    void actualizarEstado_sinApiKey_retorna401() throws Exception {
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(EstadoUsuario.SUSPENDED);
        mockMvc.perform(put("/internal/usuarios/{id}/estado", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void actualizarEstado_conApiKeyValida_retornaOk200() throws Exception {
        UUID id = UUID.randomUUID();
        ActualizarEstadoRequest request = new ActualizarEstadoRequest(EstadoUsuario.SUSPENDED);
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);

        UsuarioResponse response = new UsuarioResponse(
                id, "test@mail.escuelaing.edu.co", "Test", Set.of(RolPlataforma.STUDENT), EstadoUsuario.SUSPENDED, null
        );

        when(usuarioUseCase.cambiarEstado(eq(id), eq(EstadoUsuario.SUSPENDED))).thenReturn(usuario);
        when(usuarioRestMapper.toResponse(usuario)).thenReturn(response);

        mockMvc.perform(put("/internal/usuarios/{id}/estado", id)
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDED"));
    }

    @Test
    void obtenerPerfilMatching_sinApiKey_retorna401() throws Exception {
        mockMvc.perform(get("/internal/usuarios/{id}/perfil-matching", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void obtenerPerfilMatching_conApiKeyValida_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);
        Perfil perfil = Perfil.reconstruir(
                UUID.randomUUID(), id, "Test", "Apellido", "bio", "Ingeniería de Sistemas", null,
                5, null, null, List.of("Música", "Deportes"), Disponibilidad.DISPONIBLE, null,
                true, Instant.now());

        when(usuarioUseCase.buscarPorId(id)).thenReturn(usuario);
        when(perfilUseCase.obtenerPerfil(id)).thenReturn(perfil);

        mockMvc.perform(get("/internal/usuarios/{id}/perfil-matching", id)
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVE"))
                .andExpect(jsonPath("$.carrera").value("Ingeniería de Sistemas"))
                .andExpect(jsonPath("$.semestre").value(5))
                .andExpect(jsonPath("$.disponibilidad").value("DISPONIBLE"));
    }

    @Test
    void obtenerPerfilMatching_sinOnboardingCompleto_retorna404() throws Exception {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);

        when(usuarioUseCase.buscarPorId(id)).thenReturn(usuario);
        when(perfilUseCase.obtenerPerfil(id)).thenThrow(new PerfilNoEncontradoException(id));

        mockMvc.perform(get("/internal/usuarios/{id}/perfil-matching", id)
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarCandidatosMatching_sinApiKey_retorna401() throws Exception {
        mockMvc.perform(get("/internal/usuarios/candidatos-matching")
                        .param("excluirUsuarioId", UUID.randomUUID().toString())
                        .param("limite", "50"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void buscarCandidatosMatching_conApiKeyValida_retorna200ConEstadoActive() throws Exception {
        UUID excluirId = UUID.randomUUID();
        Perfil candidato = Perfil.reconstruir(
                UUID.randomUUID(), UUID.randomUUID(), "Ana", "Gómez", null, "Diseño Industrial", null,
                3, null, null, List.of("Cine"), Disponibilidad.OCUPADO, null, true, Instant.now());

        when(perfilUseCase.buscarCandidatos(excluirId, 50)).thenReturn(List.of(candidato));

        mockMvc.perform(get("/internal/usuarios/candidatos-matching")
                        .header(InternalApiKeyFilter.HEADER, VALID_KEY)
                        .param("excluirUsuarioId", excluirId.toString())
                        .param("limite", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("ACTIVE"))
                .andExpect(jsonPath("$[0].carrera").value("Diseño Industrial"))
                .andExpect(jsonPath("$[0].disponibilidad").value("OCUPADO"));
    }
}