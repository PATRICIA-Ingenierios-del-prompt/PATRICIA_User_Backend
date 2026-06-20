package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.FotoNoEncontradaException;
import com.escuelaing.usuarios.domain.model.AlbumFotos;
import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.in.AlbumUseCase;
import com.escuelaing.usuarios.domain.port.out.FotoRepositoryPort;
import com.escuelaing.usuarios.domain.port.out.UsuarioEventPublisherPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementación del caso de uso AlbumUseCase ("monas").
 *
 * Reglas:
 * - Máximo 6 fotos por usuario (MaxFotosException -> HTTP 409).
 * - orden = 1 es la foto principal.
 * - Al eliminar la foto principal, se promueve automáticamente la siguiente.
 */
@Service
@Transactional
public class AlbumService implements AlbumUseCase {

    private final FotoRepositoryPort fotoRepository;
    private final UsuarioEventPublisherPort eventPublisher;

    public AlbumService(FotoRepositoryPort fotoRepository, UsuarioEventPublisherPort eventPublisher) {
        this.fotoRepository = fotoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Foto> listarFotos(UUID usuarioId) {
        return fotoRepository.buscarPorUsuarioId(usuarioId);
    }

    @Override
    public Foto agregarFoto(UUID usuarioId, String urlFoto) {
        List<Foto> existentes = fotoRepository.buscarPorUsuarioId(usuarioId);
        AlbumFotos album = AlbumFotos.de(usuarioId, existentes);

        Foto nueva = album.agregarFoto(urlFoto);
        Foto guardada = fotoRepository.guardar(nueva);

        eventPublisher.publicarFotoAgregada(usuarioId, guardada.getId(), guardada.getOrden());

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

        // eliminarFoto ya reordena internamente el álbum (promoviendo la
        // siguiente foto a principal si corresponde) antes de persistir.
        album.eliminarFoto(fotoId);

        fotoRepository.eliminar(fotoId);
        // Persistir el reordenamiento contiguo del resto de fotos (incluye
        // la promoción automática de la nueva foto principal si aplica).
        fotoRepository.guardarTodas(album.getFotos());

        eventPublisher.publicarFotoEliminada(usuarioId, fotoId);
    }
}
