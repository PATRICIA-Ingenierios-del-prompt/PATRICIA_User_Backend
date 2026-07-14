package com.escuelaing.usuarios.infrastructure.scheduler;

import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Scheduler que revisa cada hora si existen cuentas en estado
 * PENDING_DELETION cuya fecha de solicitud de eliminación superó
 * las 24 horas de gracia.
 *
 * Al detectarlas: publica el evento usuario.eliminado y elimina
 * permanentemente todos los datos del usuario (las fotos se eliminan
 * en cascada por la FK con ON DELETE CASCADE definida en la BD).
 */
@Component
public class EliminacionCuentaScheduler {

    private static final Logger log = LoggerFactory.getLogger(EliminacionCuentaScheduler.class);
    private static final long HORAS_GRACIA = 24;

    private final UsuarioRepositoryPort usuarioRepository;
    private final UsuarioEventPublisherPort eventPublisher;

    public EliminacionCuentaScheduler(UsuarioRepositoryPort usuarioRepository,
                                       UsuarioEventPublisherPort eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Se ejecuta cada hora (3 600 000 ms). Busca cuentas con gracia expirada
     * y las elimina permanentemente.
     */
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void purgarCuentasPendientes() {
        Instant limite = Instant.now().minus(HORAS_GRACIA, ChronoUnit.HOURS);
        List<Usuario> pendientes = usuarioRepository.buscarPendientesDeEliminacionAnterioresA(limite);

        if (pendientes.isEmpty()) {
            return;
        }

        log.info("Purga de cuentas: {} cuenta(s) con gracia expirada encontradas", pendientes.size());

        for (Usuario usuario : pendientes) {
            try {
                // El evento se publica ANTES del borrado para que los
                // consumidores puedan reaccionar con el id todavía válido.
                eventPublisher.publicarUsuarioEliminado(usuario.getId());
                usuarioRepository.eliminarPorId(usuario.getId());
                log.info("Cuenta eliminada permanentemente: usuarioId={}", usuario.getId());
            } catch (Exception e) {
                // Si falla uno, continuar con el resto; se reintentará en el
                // siguiente ciclo porque el usuario seguirá en PENDING_DELETION.
                log.error("Error al eliminar cuenta usuarioId={}: {}", usuario.getId(), e.getMessage(), e);
            }
        }
    }
}
