package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.PerfilNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.FranjaHoraria;
import com.escuelaing.usuarios.domain.model.Genero;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.in.PerfilUseCase;
import com.escuelaing.usuarios.domain.port.outbound.FotoAlbumStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.FotoPerfilStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementación del caso de uso PerfilUseCase.
 *
 * El perfil se cachea en Redis (cache "perfiles") para lecturas frecuentes;
 * cualquier escritura invalida la entrada correspondiente.
 */
@Service
@Transactional
public class PerfilService implements PerfilUseCase {

    private static final Pattern DATA_URL_PATTERN =
            Pattern.compile("^data:(?<mime>image/[a-zA-Z0-9.+-]+);base64,(?<contenido>.+)$",
                    Pattern.DOTALL);

    private static final Map<String, String> MIME_PERMITIDOS = Map.of(
            "image/png",  "image/png",
            "image/jpeg", "image/jpeg",
            "image/jpg",  "image/jpeg",
            "image/webp", "image/webp"
    );

    private final PerfilRepositoryPort perfilRepository;
    private final UsuarioEventPublisherPort eventPublisher;
    private final FotoPerfilStoragePort fotoPerfilStorage;
    private final FotoAlbumStoragePort fotoAlbumStorage;

    public PerfilService(PerfilRepositoryPort perfilRepository,
                         UsuarioEventPublisherPort eventPublisher,
                         FotoPerfilStoragePort fotoPerfilStorage,
                         FotoAlbumStoragePort fotoAlbumStorage) {
        this.perfilRepository = perfilRepository;
        this.eventPublisher = eventPublisher;
        this.fotoPerfilStorage = fotoPerfilStorage;
        this.fotoAlbumStorage = fotoAlbumStorage;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "perfiles", key = "#usuarioId")
    public Perfil obtenerPerfil(UUID usuarioId) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));
        // El frontend usa este GET para decidir si mostrar el onboarding:
        // mientras no esté completo, se trata igual que "no existe" (404).
        if (!perfil.isOnboardingCompleto()) {
            throw new PerfilNoEncontradoException(usuarioId);
        }
        return perfil;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Perfil> buscarCandidatos(UUID excluirUsuarioId, int limite) {
        return perfilRepository.buscarCandidatos(excluirUsuarioId, limite);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Perfil> buscarUsuarios(String query, UUID excluirUsuarioId, int limite) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        int limiteSeguro = Math.min(Math.max(limite, 1), 50);
        return perfilRepository.buscarPorNombreOCarrera(query.trim(), excluirUsuarioId, limiteSeguro);
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil actualizarPerfil(UUID usuarioId, String bio, String carrera, Integer semestre,
                                   List<String> intereses, Disponibilidad disponibilidad) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        Disponibilidad disponibilidadAnterior = perfil.getDisponibilidad();
        List<String> camposModificados = perfil.actualizar(bio, carrera, semestre, intereses, disponibilidad);

        if (camposModificados.isEmpty()) {
            return perfil;
        }

        Perfil actualizado = perfilRepository.guardar(perfil);

        eventPublisher.publicarPerfilActualizado(usuarioId, camposModificados);

        if (camposModificados.contains("intereses")) {
            eventPublisher.publicarInteresesActualizados(usuarioId, actualizado.getIntereses());
        }
        if (camposModificados.contains("disponibilidad")
                && actualizado.getDisponibilidad() != disponibilidadAnterior) {
            eventPublisher.publicarDisponibilidadCambiada(usuarioId, actualizado.getDisponibilidad().name());
        }

        return actualizado;
    }

    @Override
    @Transactional(readOnly = true)
    public Disponibilidad obtenerDisponibilidad(UUID usuarioId) {
        return perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId))
                .getDisponibilidad();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> obtenerIntereses(UUID usuarioId) {
        return perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId))
                .getIntereses();
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public List<String> actualizarIntereses(UUID usuarioId, List<String> intereses) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        boolean cambio = perfil.actualizarIntereses(intereses);
        if (!cambio) {
            return perfil.getIntereses();
        }

        Perfil actualizado = perfilRepository.guardar(perfil);

        eventPublisher.publicarPerfilActualizado(usuarioId, List.of("intereses"));
        eventPublisher.publicarInteresesActualizados(usuarioId, actualizado.getIntereses());

        return actualizado.getIntereses();
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil completarOnboarding(UUID usuarioId, String nombre, String apellidos, String carrera,
                                      String segundaCarrera, Integer semestre, LocalDate fechaNacimiento,
                                      Genero genero, String fotoDataUrl, List<String> intereses) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        String urlFotoPerfil = null;
        if (fotoDataUrl != null && !fotoDataUrl.isBlank()) {
            urlFotoPerfil = fotoPerfilStorage.subirFotoPerfil(usuarioId, fotoDataUrl);
        }

        perfil.completarOnboarding(nombre, apellidos, carrera, segundaCarrera, semestre,
                fechaNacimiento, genero, urlFotoPerfil, intereses);

        Perfil guardado = perfilRepository.guardar(perfil);

        eventPublisher.publicarPerfilActualizado(usuarioId,
                List.of("nombre", "apellidos", "carrera", "semestre", "intereses", "onboardingCompleto"));
        eventPublisher.publicarInteresesActualizados(usuarioId, guardado.getIntereses());

        return guardado;
    }

    // ── foto de perfil ────────────────────────────────────────────────────────

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil actualizarFotoPerfil(UUID usuarioId, byte[] contenido, String contentType) {
        String mimeNorm = contentType == null ? "" : contentType.toLowerCase().trim();
        if (!MIME_PERMITIDOS.containsKey(mimeNorm)) {
            throw new com.escuelaing.usuarios.domain.exception.DominioInvalidoException(
                    "Formato no soportado: " + contentType + ". Use image/jpeg, image/png o image/webp.");
        }

        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        String url = fotoAlbumStorage.subirFotoAlbum(usuarioId, contenido, MIME_PERMITIDOS.get(mimeNorm));
        perfil.actualizarUrlFotoPerfil(url);
        // Al reemplazar foto, el flag de persona se reinicia
        if (perfil.isTienePersonaEnFoto()) {
            perfil.marcarPersonaDetectadaEnFoto(); // ya está en true → no hace nada
        }

        Perfil guardado = perfilRepository.guardar(perfil);
        eventPublisher.publicarPerfilActualizado(usuarioId, List.of("urlFotoPerfil"));
        return guardado;
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil actualizarFotoPerfilDesdeDataUrl(UUID usuarioId, String dataUrl) {
        Matcher matcher = DATA_URL_PATTERN.matcher(dataUrl == null ? "" : dataUrl);
        if (!matcher.matches()) {
            throw new com.escuelaing.usuarios.domain.exception.DominioInvalidoException(
                    "foto debe ser un data-URL base64 válido (data:image/<tipo>;base64,...)");
        }
        String mime = matcher.group("mime").toLowerCase();
        if (!MIME_PERMITIDOS.containsKey(mime)) {
            throw new com.escuelaing.usuarios.domain.exception.DominioInvalidoException(
                    "Formato de imagen no soportado: " + mime);
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(matcher.group("contenido"));
        } catch (IllegalArgumentException e) {
            throw new com.escuelaing.usuarios.domain.exception.DominioInvalidoException(
                    "El contenido base64 de la foto no es válido");
        }
        return actualizarFotoPerfil(usuarioId, bytes, MIME_PERMITIDOS.get(mime));
    }

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil marcarPersonaEnFotoPerfil(UUID usuarioId) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        boolean cambio = perfil.marcarPersonaDetectadaEnFoto();
        Perfil guardado = perfilRepository.guardar(perfil);

        if (cambio) {
            // Reutilizamos la misma routing key que antes, con fotoId = null
            // (la "foto" ahora es la de perfil, no una del álbum)
            eventPublisher.publicarPersonaDetectadaEnFoto(usuarioId, perfil.getId());
        }
        return guardado;
    }

    // ── franjas de disponibilidad horaria ────────────────────────────────────

    @Override
    @CacheEvict(value = "perfiles", key = "#usuarioId")
    public Perfil actualizarFranjasDisponibilidad(UUID usuarioId, List<FranjaHoraria> franjas) {
        Perfil perfil = perfilRepository.buscarPorUsuarioId(usuarioId)
                .orElseThrow(() -> new PerfilNoEncontradoException(usuarioId));

        // Crear nuevas franjas ligadas al perfilId correcto
        List<FranjaHoraria> nuevas = franjas == null ? List.of() : franjas.stream()
                .map(f -> FranjaHoraria.crear(perfil.getId(), f.getDiaSemana(),
                        f.getHoraInicio(), f.getHoraFin()))
                .toList();

        perfil.actualizarFranjasDisponibilidad(nuevas);
        Perfil guardado = perfilRepository.guardar(perfil);
        eventPublisher.publicarPerfilActualizado(usuarioId, List.of("franjasDisponibilidad"));
        return guardado;
    }
}
