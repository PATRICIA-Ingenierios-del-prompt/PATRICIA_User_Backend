package com.escuelaing.usuarios.infrastructure.storage;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.port.outbound.FotoPerfilStoragePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Sube la foto de perfil (recibida como base64 data-URL en el onboarding)
 * a S3 y devuelve la URL pública resultante.
 *
 * Formato esperado: "data:image/<subtipo>;base64,<contenido>".
 */
@Component
public class S3FotoPerfilStorageAdapter implements FotoPerfilStoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3FotoPerfilStorageAdapter.class);

    private static final Pattern DATA_URL_PATTERN =
            Pattern.compile("^data:image/(?<subtipo>[a-zA-Z0-9.+-]+);base64,(?<contenido>.+)$", Pattern.DOTALL);

    private static final Map<String, String> EXTENSIONES_PERMITIDAS = Map.of(
            "png", "png",
            "jpeg", "jpg",
            "jpg", "jpg",
            "webp", "webp"
    );

    private static final long TAMANO_MAXIMO_BYTES = 5L * 1024 * 1024; // 5 MB

    private final S3Client s3Client;
    private final String bucket;
    private final String publicUrlBase;
    private final String region;

    public S3FotoPerfilStorageAdapter(S3Client s3Client,
                                      @Value("${aws.s3.bucket}") String bucket,
                                      @Value("${aws.s3.public-url-base}") String publicUrlBase,
                                      @Value("${aws.s3.region}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.publicUrlBase = publicUrlBase;
        this.region = region;
    }

    @Override
    public String subirFotoPerfil(UUID usuarioId, String fotoDataUrl) {
        if (bucket == null || bucket.isBlank()) {
            // Aún no desplegado (no hay bucket configurado): fallamos con un
            // mensaje claro en vez de dejar que el SDK lance una excepción
            // opaca por credenciales/bucket inexistentes.
            throw new DominioInvalidoException(
                    "El almacenamiento de fotos (S3) no está configurado todavía (AWS_S3_BUCKET vacío)");
        }

        Matcher matcher = DATA_URL_PATTERN.matcher(fotoDataUrl == null ? "" : fotoDataUrl);
        if (!matcher.matches()) {
            throw new DominioInvalidoException(
                    "foto debe ser un data-URL base64 válido (data:image/<tipo>;base64,...)");
        }

        String subtipo = matcher.group("subtipo").toLowerCase();
        String extension = EXTENSIONES_PERMITIDAS.get(subtipo);
        if (extension == null) {
            throw new DominioInvalidoException("Formato de imagen no soportado: " + subtipo);
        }

        byte[] contenido;
        try {
            contenido = Base64.getDecoder().decode(matcher.group("contenido"));
        } catch (IllegalArgumentException e) {
            throw new DominioInvalidoException("El contenido base64 de la foto no es válido");
        }

        if (contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new DominioInvalidoException("La foto no puede superar los 5 MB");
        }

        String key = "perfiles/%s/%s.%s".formatted(usuarioId, UUID.randomUUID(), extension);

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("image/" + subtipo)
                        .build(),
                RequestBody.fromBytes(contenido));

        String url = construirUrlPublica(key);
        log.info("Foto de perfil subida para usuario {}: {}", usuarioId, url);
        return url;
    }

    private String construirUrlPublica(String key) {
        if (publicUrlBase != null && !publicUrlBase.isBlank()) {
            String base = publicUrlBase.endsWith("/") ? publicUrlBase.substring(0, publicUrlBase.length() - 1) : publicUrlBase;
            return base + "/" + key;
        }
        return "https://%s.s3.%s.amazonaws.com/%s".formatted(bucket, region, key);
    }
}
