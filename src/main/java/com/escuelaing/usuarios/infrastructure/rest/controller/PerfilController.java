package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarFranjasRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarInteresesRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.ActualizarPerfilRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FotoDataUrlRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.DisponibilidadResponse;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.PerfilResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.PerfilRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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

    // ── perfil ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/perfil")
    @Operation(summary = "Obtiene el perfil completo de un usuario")
    public ResponseEntity<PerfilResponse> obtenerPerfil(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(perfilUseCase.obtenerPerfil(id)));
    }

    @PutMapping("/{id}/perfil")
    @Operation(summary = "Actualiza el perfil, o completa el onboarding cuando onboardingCompleto=true")
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

    // ── foto de perfil (única) ────────────────────────────────────────────────

    @PostMapping(value = "/{id}/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Sube o reemplaza la foto de perfil (multipart/form-data, campo 'file', máx 5 MB)")
    public ResponseEntity<PerfilResponse> actualizarFotoMultipart(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        Perfil actualizado = perfilUseCase.actualizarFotoPerfil(id, file.getBytes(), file.getContentType());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    @PostMapping("/{id}/foto/base64")
    @Operation(summary = "Sube o reemplaza la foto de perfil enviando un data-URL base64")
    public ResponseEntity<PerfilResponse> actualizarFotoBase64(
            @PathVariable UUID id,
            @Valid @RequestBody FotoDataUrlRequest request) {
        Perfil actualizado = perfilUseCase.actualizarFotoPerfilDesdeDataUrl(id, request.dataUrl());
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    @PutMapping("/{id}/foto/persona")
    @Operation(summary = "Marca la foto de perfil como contenedora de una persona. "
            + "Publica el evento album.foto.persona.detectada si es la primera vez.")
    public ResponseEntity<PerfilResponse> marcarPersonaEnFoto(@PathVariable UUID id) {
        Perfil actualizado = perfilUseCase.marcarPersonaEnFotoPerfil(id);
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    // ── disponibilidad ────────────────────────────────────────────────────────

    @GetMapping("/{id}/disponibilidad")
    @Operation(summary = "Obtiene la disponibilidad general (DISPONIBLE / OCUPADO / NO_MOLESTAR)")
    public ResponseEntity<DisponibilidadResponse> obtenerDisponibilidad(@PathVariable UUID id) {
        return ResponseEntity.ok(new DisponibilidadResponse(perfilUseCase.obtenerDisponibilidad(id)));
    }

    @PutMapping("/{id}/disponibilidad/horaria")
    @Operation(summary = "Reemplaza las franjas de disponibilidad horaria del usuario. "
            + "Lista vacía = sin franjas. Máx 20 franjas.")
    public ResponseEntity<PerfilResponse> actualizarFranjasDisponibilidad(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarFranjasRequest request) {

        List<FranjaHoraria> franjas = request.franjas().stream()
                .map(f -> FranjaHoraria.crear(null, f.diaSemana(), f.horaInicio(), f.horaFin()))
                .toList();

        Perfil actualizado = perfilUseCase.actualizarFranjasDisponibilidad(id, franjas);
        return ResponseEntity.ok(mapper.toResponse(actualizado));
    }

    @GetMapping("/{id}/disponibilidad/horaria")
    @Operation(summary = "Obtiene las franjas de disponibilidad horaria del usuario")
    public ResponseEntity<PerfilResponse> obtenerFranjasDisponibilidad(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(perfilUseCase.obtenerPerfil(id)));
    }

    // ── intereses ─────────────────────────────────────────────────────────────

    @GetMapping("/{id}/intereses")
    @Operation(summary = "Obtiene los intereses del usuario")
    public ResponseEntity<List<String>> obtenerIntereses(@PathVariable UUID id) {
        return ResponseEntity.ok(perfilUseCase.obtenerIntereses(id));
    }

    @PutMapping("/{id}/intereses")
    @Operation(summary = "Actualiza los intereses del usuario")
    public ResponseEntity<List<String>> actualizarIntereses(@PathVariable UUID id,
                                                            @Valid @RequestBody ActualizarInteresesRequest request) {
        return ResponseEntity.ok(perfilUseCase.actualizarIntereses(id, request.intereses()));
    }
}
