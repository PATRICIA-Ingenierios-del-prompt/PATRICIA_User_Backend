package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.Foto;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para la gestión del álbum de fotos.
 */
public interface AlbumUseCase {

    List<Foto> listarFotos(UUID usuarioId);

    Foto agregarFoto(UUID usuarioId, String urlFoto);

    Foto actualizarFoto(UUID usuarioId, UUID fotoId, String nuevaUrl);

    void eliminarFoto(UUID usuarioId, UUID fotoId);
}
