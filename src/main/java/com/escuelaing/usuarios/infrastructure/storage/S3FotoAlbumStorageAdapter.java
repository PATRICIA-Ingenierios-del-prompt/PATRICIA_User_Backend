package com.escuelaing.usuarios.infrastructure.storage;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.port.outbound.FotoAlbumStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Map;
import java.util.UUID;

/**
 * Sube las fotos del álbum ("monas") a S3 bajo el prefijo
 * {@code album/{usuarioId}/{uuid}.<ext>} y devuelve la URL pública.
 */
@Component
public class S3FotoAlbumStorageAdapter implements FotoAlbumStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3FotoAlbumStorageAdapter.class);

    private static final Map<String, String> MIME_A_EXTENSION = Map.of(
            "image/png",  "png",
            "image/jpeg", "jpg",
            "image/jpg",  "jpg",
            "image/webp", "webp"
    );

    private static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5 MB

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrlBase;
    private final String region;

    public S3FotoAlbumStorageAdapter(S3Client s3Client,
                                     @Value("${aws.s3.bucket}") String bucket,
                                     @Value("${aws.s3.public-url-base}") String publicUrlBase,
                                     @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicUrlBase = publicUrlBase;
        this.region = region;
    }

    @Override
    public String subirFotoAlbum(UUID usuarioId, byte[] contenido, String contentType) {
        if (bucket == null || bucket.isBlank()) {
            throw new DominioInvalidoException(
                    "El almacenamiento de fotos (S3) no está configurado todavía (AWS_S3_BUCKET vacío)");
        }

        if (contenido == null || contenido.length == 0) {
            throw new DominioInvalidoException("El contenido de la foto no puede estar vacío");
        }

        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new DominioInvalidoException("La foto no puede superar los 5 MB");
        }

        String mimeNormalizado = contentType == null ? "" : contentType.toLowerCase().trim();
        String extension = MIME_A_EXTENSION.get(mimeNormalizado);
        if (extension == null) {
            throw new DominioInvalidoException(
                    "Formato de imagen no soportado: " + contentType
                    + ". Use image/jpeg, image/png o image/webp.");
        }

        String key = "album/%s/%s.%s".formatted(usuarioId, UUID.randomUUID(), extension);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType(mimeNormalizado)
                        .build(),
                RequestBody.fromBytes(contenido));

        String url = construirUrlPublica(key);
        log.info("Foto de álbum subida para usuario {}: {}", usuarioId, url);
        return url;
    }

    private String construirUrlPublica(String key) {
        if (publicUrlBase != null && !publicUrlBase.isBlank()) {
            String base = publicUrlBase.endsWith("/")
                    ? publicUrlBase.substring(0, publicUrlBase.length() - 1)
                    : publicUrlBase;
            return base + "/" + key;
        }
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }
}
