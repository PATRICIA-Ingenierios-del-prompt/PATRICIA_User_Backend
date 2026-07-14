package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.LogrosUsuario;
import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.LogrosResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.LogroRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoint público (JWT requerido) para el álbum de logros ("monas") de un
 * usuario. No confundir con el álbum de fotos de perfil, expuesto por
 * AlbumController bajo /fotos.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Logros", description = "Catálogo de logros del álbum de \"monas\"")
public class LogroController {

    private final LogroUseCase logroUseCase;
    private final LogroRestMapper mapper;

    public LogroController(LogroUseCase logroUseCase, LogroRestMapper mapper) {
        this.logroUseCase = logroUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}/logros")
    @Operation(summary = "Obtiene el catálogo de logros de un usuario, con su estado de desbloqueo y XP total")
    public ResponseEntity<LogrosResponse> obtenerLogros(@PathVariable UUID id) {
        LogrosUsuario logros = logroUseCase.obtenerLogros(id);
        return ResponseEntity.ok(mapper.toResponse(logros));
    }
}
