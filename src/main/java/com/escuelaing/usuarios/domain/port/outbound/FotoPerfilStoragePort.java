package com.escuelaing.usuarios.domain.port.outbound;

import java.util.UUID;

/**
 * Puerto de salida para subir la foto de perfil recibida en el onboarding
 * (como base64 data-URL) a almacenamiento externo (p. ej. S3) y obtener la
 * URL pública resultante.
 */
public interface FotoPerfilStoragePort {

    /**
     * Sube la foto de un usuario y devuelve la URL pública para guardar en
     * `perfiles.url_foto_perfil`.
     *
     * @param usuarioId    id del usuario dueño de la foto.
     * @param fotoDataUrl  contenido en formato data-URL, p. ej.
     *                     "data:image/png;base64,iVBORw0KG...".
     * @return URL pública de la foto ya subida.
     */
    String subirFotoPerfil(UUID usuarioId, String fotoDataUrl);
}
