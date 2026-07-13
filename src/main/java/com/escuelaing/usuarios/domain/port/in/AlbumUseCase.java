package com.escuelaing.usuarios.domain.port.in;

import com.escuelaing.usuarios.domain.model.Foto;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de entrada (caso de uso) para la gestión del álbum de fotos.
 */
public interface AlbumUseCase {

    List<Foto> listarFotos(UUID usuarioId);

    /**
     * Agrega una foto al álbum subiendo el contenido binario a S3.
     *
     * @param usuarioId   propietario del álbum.
     * @param contenido   bytes de la imagen.
     * @param contentType MIME type (image/jpeg, image/png, image/webp).
     * @return la foto persistida con la URL de S3.
     */
    Foto agregarFoto(UUID usuarioId, byte[] contenido, String contentType);

    /**
     * Agrega una foto a partir de un data-URL base64
     * {@code data:image/<tipo>;base64,<contenido>}.
     */
    Foto agregarFotoDesdeDataUrl(UUID usuarioId, String dataUrl);

    Foto actualizarFoto(UUID usuarioId, UUID fotoId, String nuevaUrl);

    void eliminarFoto(UUID usuarioId, UUID fotoId);

    /**
     * Marca la foto como "con persona detectada" y publica el evento
     * {@code album.foto.persona.detectada} si el atributo cambia de false a true.
     *
     * @return la foto actualizada.
     */
    Foto marcarPersonaEnFoto(UUID usuarioId, UUID fotoId);
}
