package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarInteresesRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarPerfilRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.DisponibilidadResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.PerfilRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints públicos (JWT requerido) para gestión del perfil y de los
 * intereses de un usuario.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Perfil", description = "Gestión del perfil de usuario")
public class PerfilController {

    private final PerfilUseCase perfilUseCase;
    private final PerfilRestMapper mapper;

    public PerfilController(PerfilUseCase perfilUseCase, PerfilRestMapper mapper) {
        this.perfilUseCase = perfilUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}/perfil")
    @Operation(summary = "Obtiene el perfil de un usuario")
    public ResponseEntity<PerfilResponse> obtenerPerfil(@PathVariable UUID id) {
        Perfil perfil = perfilUseCase.obtenerPerfil(id);
        return ResponseEntity.ok(mapper.toResponse(perfil));
    }

    @PutMapping("/{id}/perfil")
    @Operation(summary = "Actualiza el perfil de un usuario, o completa el onboarding por primera vez "
            + "cuando onboardingCompleto=true")
    public ResponseEntity<PerfilResponse> actualizarPerfil(@PathVariable UUID id,
                                                           @Valid @RequestBody ActualizarPerfilRequest request) {
        Perfil resultado;
        if (Boolean.TRUE.equals(request.onboardingCompleto())) {
            resultado = perfilUseCase.completarOnboarding(
                    id, request.nombre(), request.apellidos(), request.carrera(),
                    request.segundaCarrera(), request.semestre(), request.fechaNacimiento(),
                    request.genero(), request.foto(), request.intereses());
        } else {
            resultado = perfilUseCase.actualizarPerfil(
                    id, request.bio(), request.carrera(), request.semestre(),
                    request.intereses(), request.disponibilidad());
        }
        return ResponseEntity.ok(mapper.toResponse(resultado));
    }

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Obtiene la disponibilidad actual de un usuario")
    public ResponseEntity<DisponibilidadResponse> obtenerDisponibilidad(@PathVariable UUID id) {
        Disponibilidad disponibilidad = perfilUseCase.obtenerDisponibilidad(id);
        return ResponseEntity.ok(new DisponibilidadResponse(disponibilidad));
    }

    @GetMapping("/{id}/intereses")
    @Operation(summary = "Obtiene los intereses de un usuario")
    public ResponseEntity<List<String>> obtenerIntereses(@PathVariable UUID id) {
        return ResponseEntity.ok(perfilUseCase.obtenerIntereses(id));
    }

    @PutMapping("/{id}/intereses")
    @Operation(summary = "Actualiza los intereses de un usuario")
    public ResponseEntity<List<String>> actualizarIntereses(@PathVariable UUID id,
                                                            @Valid @RequestBody ActualizarInteresesRequest request) {
        List<String> actualizados = perfilUseCase.actualizarIntereses(id, request.intereses());
        return ResponseEntity.ok(actualizados);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Busca usuarios por nombre, apellidos o carrera, entre todos los usuarios "
            + "ACTIVE de la plataforma (no limitado a sugerencias de matching). Excluye al usuario "
            + "autenticado de sus propios resultados.")
    public ResponseEntity<List<PerfilResponse>> buscarUsuarios(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "20") int limite,
            Authentication auth) {
        UUID usuarioId = usuarioIdAutenticado(auth);
        List<Perfil> resultados = perfilUseCase.buscarUsuarios(query, usuarioId, limite);
        List<PerfilResponse> respuesta = resultados.stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(respuesta);
    }

    private UUID usuarioIdAutenticado(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UUID uuid)) {
            throw new AccessDeniedException("No se pudo determinar el usuario autenticado");
        }
        return uuid;
    }
}
