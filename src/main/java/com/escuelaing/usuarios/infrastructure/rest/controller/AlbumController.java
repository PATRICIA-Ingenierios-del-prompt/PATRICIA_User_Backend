package com.escuelaing.usuarios.infrastructure.rest.controller;

import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.in.AlbumUseCase;
import com.escuelaing.usuarios.infrastructure.rest.dto.request.FotoDataUrlRequest;
import com.escuelaing.usuarios.infrastructure.rest.dto.response.FotoResponse;
import com.escuelaing.usuarios.infrastructure.rest.mapper.FotoRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

/**
 * Endpoints públicos (JWT requerido) para el álbum de fotos ("monas").
 * Máximo 6 fotos; orden 1 = foto principal.
 *
 * Subida de fotos:
 *   - POST /{id}/fotos          multipart/form-data  campo "file"
 *   - POST /{id}/fotos/base64   JSON  { "dataUrl": "data:image/...;base64,..." }
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
        return ResponseEntity.ok(mapper.toResponseList(albumUseCase.listarFotos(id)));
    }

    @PostMapping(value = "/{id}/fotos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agrega una foto al álbum subiendo el archivo directamente (máximo 6, 5 MB)")
    public ResponseEntity<FotoResponse> agregarFotoMultipart(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {

        byte[] bytes = file.getBytes();
        String contentType = file.getContentType();
        Foto creada = albumUseCase.agregarFoto(id, bytes, contentType);
        return ResponseEntity.status(201).body(mapper.toResponse(creada));
    }

    @PostMapping("/{id}/fotos/base64")
    @Operation(summary = "Agrega una foto al álbum enviando un data-URL base64 en el cuerpo JSON")
    public ResponseEntity<FotoResponse> agregarFotoBase64(
            @PathVariable UUID id,
            @Valid @RequestBody FotoDataUrlRequest request) {

        Foto creada = albumUseCase.agregarFotoDesdeDataUrl(id, request.dataUrl());
        return ResponseEntity.status(201).body(mapper.toResponse(creada));
    }

    @PutMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Actualiza la URL de una foto del álbum")
    public ResponseEntity<FotoResponse> actualizarFoto(@PathVariable UUID id,
                                                        @PathVariable UUID fotoId,
                                                        @RequestParam String nuevaUrl) {
        Foto actualizada = albumUseCase.actualizarFoto(id, fotoId, nuevaUrl);
        return ResponseEntity.ok(mapper.toResponse(actualizada));
    }

    @DeleteMapping("/{id}/fotos/{fotoId}")
    @Operation(summary = "Elimina una foto del álbum (promueve la siguiente si era la principal)")
    public ResponseEntity<Void> eliminarFoto(@PathVariable UUID id, @PathVariable UUID fotoId) {
        albumUseCase.eliminarFoto(id, fotoId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/fotos/{fotoId}/persona")
    @Operation(summary = "Marca la foto como contenedora de una persona. "
            + "Activa el atributo tienePersonaEnFoto y publica el evento correspondiente.")
    public ResponseEntity<FotoResponse> marcarPersonaEnFoto(@PathVariable UUID id,
                                                             @PathVariable UUID fotoId) {
        Foto actualizada = albumUseCase.marcarPersonaEnFoto(id, fotoId);
        return ResponseEntity.ok(mapper.toResponse(actualizada));
    }
}
