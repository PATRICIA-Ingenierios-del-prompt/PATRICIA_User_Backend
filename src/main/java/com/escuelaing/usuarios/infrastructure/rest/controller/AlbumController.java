package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.in.AlbumUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FotoRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.FotoResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.FotoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints públicos (JWT requerido) para el álbum de fotos ("monas") de
 * un usuario. Máximo 6 fotos; orden 1 = foto principal.
 */
@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Álbum", description = "Gestión del álbum de fotos (\"monas\")")
public class AlbumController {

    private final AlbumUseCase albumUseCase;
    private final FotoRestMapper mapper;

    public AlbumController(AlbumUseCase albumUseCase, FotoRestMapper mapper) {
        this.albumUseCase = albumUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/{id}/fotos")
    @Operation(summary = "Lista las fotos del álbum de un usuario")
    public ResponseEntity<List<FotoResponse>> listarFotos(@PathVariable UUID id) {
        List<Foto> fotos = albumUseCase.listarFotos(id);
        return ResponseEntity.ok(mapper.toResponseList(fotos));
    }

    @PostMapping("/{id}/fotos")
    @Operation(summary = "Agrega una foto al álbum (máximo 6)")
    public ResponseEntity<FotoResponse> agregarFoto(@PathVariable UUID id,
                                                      @Valid @RequestBody FotoRequest request) {
        Foto creada = albumUseCase.agregarFoto(id, request.urlFoto());
        return ResponseEntity.status(201).body(mapper.toResponse(creada));
    }

    @PutMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Actualiza la URL de una foto del álbum")
    public ResponseEntity<FotoResponse> actualizarFoto(@PathVariable UUID id,
                                                         @PathVariable UUID fotoId,
                                                         @Valid @RequestBody FotoRequest request) {
        Foto actualizada = albumUseCase.actualizarFoto(id, fotoId, request.urlFoto());
        return ResponseEntity.ok(mapper.toResponse(actualizada));
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Elimina una foto del álbum (promueve la siguiente si era la principal)")
    public ResponseEntity<Void> eliminarFoto(@PathVariable UUID id, @PathVariable UUID fotoId) {
        albumUseCase.eliminarFoto(id, fotoId);
        return ResponseEntity.noContent().build();
    }
}
