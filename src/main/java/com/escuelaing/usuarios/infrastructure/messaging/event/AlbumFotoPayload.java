package com.escuelaing.usuarios.infrastructure.messaging.event;

import java.util.UUID;

public record AlbumFotoPayload(
        UUID fotoId,
        Integer orden
) {
    public static AlbumFotoPayload agregada(UUID fotoId, int orden) {
        return new AlbumFotoPayload(fotoId, orden);
    }

    public static AlbumFotoPayload eliminada(UUID fotoId) {
        return new AlbumFotoPayload(fotoId, null);
    }
}
