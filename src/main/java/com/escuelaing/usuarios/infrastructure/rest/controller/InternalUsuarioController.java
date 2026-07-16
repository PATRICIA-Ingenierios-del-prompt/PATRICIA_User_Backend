package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarEstadoRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FindOrCreateRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.FranjaHorariaResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilMatchingResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.UsuarioResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.UsuarioRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints internos consumidos por auth-service, matching-service y otros
 * microservicios, protegidos exclusivamente por el header
 * X-Internal-Api-Key (ver InternalApiKeyFilter). NO usan JWT.
 *
 * Contratos NO CAMBIAR (definidos por auth-service):
 * - POST /internal/usuarios/find-or-create
 * - GET  /internal/usuarios/{id}
 *
 * Contratos consumidos por matching-service (ver UsuarioFeignClient en el
 * repo de matching-service):
 * - GET /internal/usuarios/{id}/perfil-matching
 * - GET /internal/usuarios/candidatos-matching
 */
@RestController
@RequestMapping("/internal/usuarios")
@Tag(name = "Internal - Usuarios", description = "Endpoints internos servicio-a-servicio")
public class InternalUsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final PerfilUseCase perfilUseCase;
    private final UsuarioRestMapper mapper;

    public InternalUsuarioController(UsuarioUseCase usuarioUseCase, PerfilUseCase perfilUseCase,
                                      UsuarioRestMapper mapper) {
        this.usuarioUseCase = usuarioUseCase;
        this.perfilUseCase = perfilUseCase;
        this.mapper = mapper;
    }

    @PostMapping("/find-or-create")
    @Operation(summary = "Busca un usuario por email o lo crea (idempotente). Usado por auth-service.")
    public ResponseEntity<UsuarioResponse> findOrCreate(@Valid @RequestBody FindOrCreateRequest request) {
        UsuarioUseCase.ResultadoFindOrCreate resultado = usuarioUseCase.buscarOCrear(
                request.email(), request.nombre(), request.microsoftId());

        UsuarioResponse response = mapper.toResponse(resultado.usuario());
        return resultado.creado()
                ? ResponseEntity.status(201).body(response)
                : ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene un usuario por id. Usado por auth-service.")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable UUID id) {
        Usuario usuario = usuarioUseCase.buscarPorId(id);
        return ResponseEntity.ok(mapper.toResponse(usuario));
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca un usuario por email")
    public ResponseEntity<UsuarioResponse> buscarPorEmail(@RequestParam String email) {
        return usuarioUseCase.buscarPorEmail(email)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado de un usuario (ACTIVE, SUSPENDED, BANNED)")
    public ResponseEntity<UsuarioResponse> actualizarEstado(@PathVariable UUID id,
                                                              @Valid @RequestBody ActualizarEstadoRequest request) {
        Usuario actualizado = usuarioUseCase.cambiarEstado(id, request.estado());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    @GetMapping("/{id}/perfil-matching")
    @Operation(summary = "Perfil combinado (estado + carrera/semestre/intereses/disponibilidad) "
            + "para el cálculo de matching. Usado por matching-service. 404 si no existe perfil "
            + "o el onboarding no está completo (mismo criterio que /api/v1/usuarios/{id}/perfil).")
    public ResponseEntity<PerfilMatchingResponse> obtenerPerfilMatching(@PathVariable UUID id) {
        Usuario usuario = usuarioUseCase.buscarPorId(id);
        Perfil perfil = perfilUseCase.obtenerPerfil(id);
        return ResponseEntity.ok(aPerfilMatchingResponse(usuario.getEstado().name(), perfil));
    }

    @GetMapping("/candidatos-matching")
    @Operation(summary = "Pool de candidatos elegibles para matching (usuarios ACTIVE con onboarding "
            + "completo), excluyendo al usuario que los solicita. Usado por matching-service.")
    public ResponseEntity<List<PerfilMatchingResponse>> buscarCandidatosMatching(
            @RequestParam UUID excluirUsuarioId,
            @RequestParam(defaultValue = "50") int limite) {
        // La consulta ya filtra por estado ACTIVE (join con `usuarios`), así que
        // todo lo que llega aquí cumple esa condición sin necesidad de otro lookup.
        List<PerfilMatchingResponse> candidatos = perfilUseCase.buscarCandidatos(excluirUsuarioId, limite).stream()
                .map(perfil -> aPerfilMatchingResponse("ACTIVE", perfil))
                .toList();
        return ResponseEntity.ok(candidatos);
    }

    private PerfilMatchingResponse aPerfilMatchingResponse(String estado, Perfil perfil) {
        List<FranjaHorariaResponse> franjas = perfil.getFranjasDisponibilidad() == null
                ? List.of()
                : perfil.getFranjasDisponibilidad().stream()
                        .map(f -> new FranjaHorariaResponse(
                                f.getId(), f.getDiaSemana(), f.getHoraInicio(), f.getHoraFin()))
                        .toList();

        return new PerfilMatchingResponse(
                perfil.getUsuarioId(),
                estado,
                perfil.getIntereses(),
                perfil.getCarrera(),
                perfil.getSemestre(),
                perfil.getDisponibilidad().name(),
                perfil.getUrlFotoPerfil(),
                perfil.isTienePersonaEnFoto(),
                franjas
        );
    }
}
