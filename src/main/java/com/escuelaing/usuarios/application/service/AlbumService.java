package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.exception.FotoNoEncontradaException;
import com.escuelaing.usuarios.domain.exception.MaxFotosException;
import com.escuelaing.usuarios.domain.model.AlbumFotos;
import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.in.AlbumUseCase;
import com.escuelaing.usuarios.domain.port.outbound.FotoAlbumStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.FotoRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación del caso de uso AlbumUseCase ("monas").
 *
 * Reglas:
 * - Máximo 6 fotos por usuario (MaxFotosException → HTTP 409).
 * - orden = 1 es la foto principal.
 * - Al eliminar la foto principal, se promueve automáticamente la siguiente.
 * - Las fotos se suben a S3 (bytes o data-URL base64).
 * - Si se detecta una persona, se marca el atributo y se publica el evento.
 */
@Service
@Transactional
public class AlbumService implements AlbumUseCase {

    private static final Pattern DATA_URL_PATTERN =
            Pattern.compile("^data:(?<mime>image/[a-zA-Z0-9.+-]+);base64,(?<contenido>.+)$",
                    Pattern.DOTALL);

    private static final Map<String, String> MIME_PERMITIDOS = Map.of(
            "image/png",  "image/png",
            "image/jpeg", "image/jpeg",
            "image/jpg",  "image/jpeg",
            "image/webp", "image/webp"
    );

    private final FotoRepositoryPort fotoRepository;
    private final FotoAlbumStoragePort fotoStorage;
    private final UsuarioEventPublisherPort eventPublisher;

    public AlbumService(FotoRepositoryPort fotoRepository,
                        FotoAlbumStoragePort fotoStorage,
                        UsuarioEventPublisherPort eventPublisher) {
        this.fotoRepository = fotoRepository;
        this.fotoStorage = fotoStorage;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Foto> listarFotos(UUID usuarioId) {
        return fotoRepository.buscarPorUsuarioId(usuarioId);
    }

    @Override
    public Foto agregarFoto(UUID usuarioId, byte[] contenido, String contentType) {
        String mimeNorm = contentType == null ? "" : contentType.toLowerCase().trim();
        if (!MIME_PERMITIDOS.containsKey(mimeNorm)) {
            throw new DominioInvalidoException(
                    "Formato no soportado: " + contentType + ". Use image/jpeg, image/png o image/webp.");
        }

        // Validar límite ANTES de subir a S3 para no desperdiciar la llamada
        List<Foto> existentes = fotoRepository.buscarPorUsuarioId(usuarioId);
        AlbumFotos album = AlbumFotos.de(usuarioId, existentes);
        if (album.size() >= AlbumFotos.MAX_FOTOS) {
            throw new MaxFotosException(AlbumFotos.MAX_FOTOS);
        }

        String urlFoto = fotoStorage.subirFotoAlbum(usuarioId, contenido, MIME_PERMITIDOS.get(mimeNorm));
        return persistirNuevaFoto(album, urlFoto);
    }

    @Override
    public Foto agregarFotoDesdeDataUrl(UUID usuarioId, String dataUrl) {
        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl == null ? "" : dataUrl);
        if (!matcher.matches()) {
            throw new DominioInvalidoException(
                    "foto debe ser un data-URL base64 válido (data:image/<tipo>;base64,...)");
        }

        String mime = matcher.group("mime").toLowerCase();
        if (!MIME_PERMITIDOS.containsKey(mime)) {
            throw new DominioInvalidoException("Formato de imagen no soportado: " + mime);
        }

        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(matcher.group("contenido"));
        } catch (IllegalArgumentException e) {
            throw new DominioInvalidoException("El contenido base64 de la foto no es válido");
        }

        // Validar límite ANTES de subir a S3
        List<Foto> existentes = fotoRepository.buscarPorUsuarioId(usuarioId);
        AlbumFotos album = AlbumFotos.de(usuarioId, existentes);
        if (album.size() >= AlbumFotos.MAX_FOTOS) {
            throw new MaxFotosException(AlbumFotos.MAX_FOTOS);
        }

        String urlFoto = fotoStorage.subirFotoAlbum(usuarioId, bytes, MIME_PERMITIDOS.get(mime));
        return persistirNuevaFoto(album, urlFoto);
    }

    private Foto persistirNuevaFoto(AlbumFotos album, String urlFoto) {
        Foto nueva = album.agregarFoto(urlFoto);
        Foto guardada = fotoRepository.guardar(nueva);

        eventPublisher.publicarFotoAgregada(album.getUsuarioId(), guardada.getId(), guardada.getOrden());

        return guardada;
    }

    @Override
    public Foto actualizarFoto(UUID usuarioId, UUID fotoId, String nuevaUrl) {
        List<Foto> existentes = fotoRepository.buscarPorUsuarioId(usuarioId);

        Foto foto = existentes.stream()
                .filter(f -> f.getId().equals(fotoId))
                .findFirst()
                .orElseThrow(() -> new FotoNoEncontradaException(fotoId));

        foto.actualizarUrl(nuevaUrl);
        return fotoRepository.guardar(foto);
    }

    @Override
    public void eliminarFoto(UUID usuarioId, UUID fotoId) {
        List<Foto> existentes = fotoRepository.buscarPorUsuarioId(usuarioId);
        AlbumFotos album = AlbumFotos.de(usuarioId, existentes);

        boolean existia = existentes.stream().anyMatch(f -> f.getId().equals(fotoId));
        if (!existia) {
            throw new FotoNoEncontradaException(fotoId);
        }

        album.eliminarFoto(fotoId);

        fotoRepository.eliminar(fotoId);
        fotoRepository.guardarTodas(album.getFotos());

        eventPublisher.publicarFotoEliminada(usuarioId, fotoId);
    }

    @Override
    public Foto marcarPersonaEnFoto(UUID usuarioId, UUID fotoId) {
        Foto foto = fotoRepository.buscarPorId(fotoId)
                .orElseThrow(() -> new FotoNoEncontradaException(fotoId));

        // Validar que la foto pertenece al usuario
        if (!foto.getUsuarioId().equals(usuarioId)) {
            throw new FotoNoEncontradaException(fotoId);
        }

        boolean cambio = foto.marcarPersonaDetectada();
        Foto guardada = fotoRepository.guardar(foto);

        if (cambio) {
            eventPublisher.publicarPersonaDetectadaEnFoto(usuarioId, fotoId);
        }

        return guardada;
    }
}
