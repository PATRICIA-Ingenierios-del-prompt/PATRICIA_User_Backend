package com.escuelaing.usuarios.domain.port.outbound;

import java.util.UUID;

/**
 * Puerto de salida para el almacenamiento de las fotos del álbum ("monas")
 * en S3. Implementado por S3FotoAlbumStorageAdapter en infrastructure.storage.
 */
public interface FotoAlbumStoragePort {

    /**
     * Sube una imagen al bucket S3 bajo el prefijo {@code album/{usuarioId}/}
     * y devuelve la URL pública resultante.
     *
     * @param usuarioId   propietario del álbum.
     * @param contenido   bytes de la imagen.
     * @param contentType MIME type (image/jpeg, image/png, image/webp).
     * @return URL pública de la imagen en S3 (o CloudFront si está configurado).
     */
    String subirFotoAlbum(UUID usuarioId, byte[] contenido, String contentType);
}
