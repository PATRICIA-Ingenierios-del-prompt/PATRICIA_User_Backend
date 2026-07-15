package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.CategoriaActividad;
import com.escuelaing.usuarios.domain.model.Logro;
import com.escuelaing.usuarios.domain.model.LogroTipo;
import com.escuelaing.usuarios.domain.model.LogrosUsuario;
import com.escuelaing.usuarios.domain.port.in.LogroUseCase;
import com.escuelaing.usuarios.domain.port.outbound.BienestarPort;
import com.escuelaing.usuarios.domain.port.outbound.LogroEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.LogroRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.LogroSenalRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del caso de uso LogroUseCase: el motor de reglas del álbum
 * de logros ("monas").
 *
 * Bienestar no publica eventos, así que Mona Respira / Mona Tranquila se
 * reevalúan de dos formas combinadas: de forma perezosa cada vez que se pide
 * el álbum ({@link #obtenerLogros}), y de forma oportunista cada vez que
 * llega cualquier otra señal de actividad (parche/evento/match) del mismo
 * usuario. Un corte temprano evita llamar al servicio de bienestar una vez
 * que ambos logros de bienestar ya están desbloqueados.
 */
@Service
@Transactional
public class LogroService implements LogroUseCase {

    private static final Logger log = LoggerFactory.getLogger(LogroService.class);

    private static final Map<CategoriaActividad, List<LogroTipo>> PARCHE_A_LOGROS = Map.of(
            CategoriaActividad.TECHNOLOGY, List.of(LogroTipo.MONA_CODER),
            CategoriaActividad.MUSIC, List.of(LogroTipo.MONA_DJ, LogroTipo.MONA_MUSICA),
            CategoriaActividad.ENTERTAINMENT, List.of(LogroTipo.MONA_CULTURA, LogroTipo.MONA_GAMER),
            CategoriaActividad.ART, List.of(LogroTipo.MONA_ARTE),
            CategoriaActividad.SPORT, List.of(LogroTipo.MONA_AIRE_LIBRE),
            CategoriaActividad.VARIETY, List.of(LogroTipo.MONA_FOODIE)
    );

    private static final Map<CategoriaActividad, List<LogroTipo>> EVENTO_A_LOGROS = Map.of(
            CategoriaActividad.MUSIC, List.of(LogroTipo.MONA_DJ, LogroTipo.MONA_MUSICA),
            CategoriaActividad.ENTERTAINMENT, List.of(LogroTipo.MONA_CULTURA),
            CategoriaActividad.ART, List.of(LogroTipo.MONA_ARTE),
            CategoriaActividad.VARIETY, List.of(LogroTipo.MONA_CIENTIFICA)
    );

    private static final long UMBRAL_ESTUDIOSA = 3;
    private static final long UMBRAL_MATCHES_SOCIAL = 10;
    private static final int UMBRAL_EJERCICIOS_BIENESTAR = 3;

    private final UsuarioRepositoryPort usuarioRepository;
    private final LogroRepositoryPort logroRepository;
    private final LogroSenalRepositoryPort senalRepository;
    private final BienestarPort bienestarPort;
    private final LogroEventPublisherPort eventPublisher;

    public LogroService(UsuarioRepositoryPort usuarioRepository,
                        LogroRepositoryPort logroRepository,
                        LogroSenalRepositoryPort senalRepository,
                        BienestarPort bienestarPort,
                        LogroEventPublisherPort eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.logroRepository = logroRepository;
        this.senalRepository = senalRepository;
        this.bienestarPort = bienestarPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional(readOnly = false)
    public LogrosUsuario obtenerLogros(UUID usuarioId) {
        usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        evaluarBienestarYCompuestos(usuarioId);

        Map<LogroTipo, Instant> desbloqueados = logroRepository.buscarDesbloqueadosConFecha(usuarioId);
        int xpTotal = logroRepository.calcularXpTotal(usuarioId);

        List<Logro> catalogo = Arrays.stream(LogroTipo.values())
                .map(tipo -> new Logro(tipo, desbloqueados.containsKey(tipo), desbloqueados.get(tipo)))
                .toList();

        return new LogrosUsuario(usuarioId, xpTotal, catalogo);
    }

    @Override
    public void procesarActividadParche(UUID usuarioId, UUID parcheId, String categoriaStr) {
        CategoriaActividad.fromExterno(categoriaStr).ifPresent(categoria -> {
            PARCHE_A_LOGROS.getOrDefault(categoria, List.of())
                    .forEach(tipo -> otorgarSiNuevo(usuarioId, tipo));

            if (categoria == CategoriaActividad.STUDY) {
                if (senalRepository.registrarParche(usuarioId, parcheId, categoria)
                        && senalRepository.contarParchesDistintosPorCategoria(usuarioId, categoria) >= UMBRAL_ESTUDIOSA) {
                    otorgarSiNuevo(usuarioId, LogroTipo.MONA_ESTUDIOSA);
                }
            } else if (categoria == CategoriaActividad.VARIETY) {
                senalRepository.registrarParche(usuarioId, parcheId, categoria);
            }
        });

        evaluarBienestarYCompuestos(usuarioId);
    }

    @Override
    public void procesarActividadEvento(UUID usuarioId, UUID eventoId, String categoriaStr) {
        CategoriaActividad.fromExterno(categoriaStr).ifPresent(categoria ->
                EVENTO_A_LOGROS.getOrDefault(categoria, List.of())
                        .forEach(tipo -> otorgarSiNuevo(usuarioId, tipo)));

        evaluarBienestarYCompuestos(usuarioId);
    }

    @Override
    public void procesarMatchConfirmado(UUID usuarioId, UUID matchId) {
        if (senalRepository.registrarMatch(usuarioId, matchId)
                && senalRepository.contarMatches(usuarioId) > UMBRAL_MATCHES_SOCIAL) {
            otorgarSiNuevo(usuarioId, LogroTipo.MONA_SOCIAL);
        }

        evaluarBienestarYCompuestos(usuarioId);
    }

    private void evaluarBienestarYCompuestos(UUID usuarioId) {
        boolean respiraListo = logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_RESPIRA);
        boolean tranquilaListo = logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_TRANQUILA);
        if (respiraListo && tranquilaListo) {
            return;
        }

        Optional<Integer> conteo = bienestarPort.contarEjerciciosCompletados(usuarioId);
        if (conteo.isEmpty() || conteo.get() < UMBRAL_EJERCICIOS_BIENESTAR) {
            return;
        }

        if (!respiraListo) {
            otorgarSiNuevo(usuarioId, LogroTipo.MONA_RESPIRA);
        }
        if (!tranquilaListo && senalRepository.existeParcheCategoria(usuarioId, CategoriaActividad.VARIETY)) {
            otorgarSiNuevo(usuarioId, LogroTipo.MONA_TRANQUILA);
        }
    }

    private void otorgarSiNuevo(UUID usuarioId, LogroTipo tipo) {
        if (logroRepository.otorgarSiNoExiste(usuarioId, tipo, tipo.getXp())) {
            int xpTotal = logroRepository.calcularXpTotal(usuarioId);
            log.info("Logro: {} otorgado a usuario {} (+{} XP, total {})",
                    tipo.name(), usuarioId, tipo.getXp(), xpTotal);
            eventPublisher.publicarLogroDesbloqueado(usuarioId, tipo, tipo.getXp(), xpTotal);
        }
    }
}
