package com.escuelaing.usuarios.domain.port.outbound;

import com.escuelaing.usuarios.domain.model.Foto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de salida para la persistencia de las fotos del álbum.
 */
public interface FotoRepositoryPort {

    List<Foto> buscarPorUsuarioId(UUID usuarioId);

    Optional<Foto> buscarPorId(UUID fotoId);

    Foto guardar(Foto foto);

    void eliminar(UUID fotoId);

    /**
     * Reemplaza de forma atómica el conjunto de fotos de un usuario,
     * usado tras reordenar el álbum (p. ej. al eliminar la foto principal).
     */
    void guardarTodas(List<Foto> fotos);
}
