package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.ReporteUseCase;
import com.escuelaing.usuarios.domain.port.outbound.AuthServicePort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso ReporteUseCase, invocado por el consumer
 * de la cola usuarios.reportes (exchange patricia.parches, routing key
 * reporte.emitido).
 *
 * Regla: al alcanzar el umbral configurado de reportes, el usuario se
 * suspende, se publica usuario.suspendido y se cierran sus sesiones activas
 * mediante una llamada Feign a auth-service.
 */
@Service
@Transactional
public class ReporteService implements ReporteUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final UsuarioEventPublisherPort eventPublisher;
    private final AuthServicePort authServicePort;
    private final int maxReportesAntesSuspension;

    public ReporteService(UsuarioRepositoryPort usuarioRepository,
                           UsuarioEventPublisherPort eventPublisher,
                           AuthServicePort authServicePort,
                           @Value("${reportes.max-antes-suspension:5}") int maxReportesAntesSuspension) {
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
        this.authServicePort = authServicePort;
        this.maxReportesAntesSuspension = maxReportesAntesSuspension;
    }

    @Override
    public void registrarReporte(UUID usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        usuario.incrementarReportes();

        boolean debeSuspenderse = usuario.debeSerSuspendidoPorReportes(maxReportesAntesSuspension);
        if (debeSuspenderse) {
            usuario.suspender();
        }

        Usuario actualizado = usuarioRepository.guardar(usuario);

        if (debeSuspenderse) {
            eventPublisher.publicarUsuarioSuspendido(
                    actualizado.getId(), MotivoSuspension.REPORTES, actualizado.getContadorReportes());
            authServicePort.cerrarSesion(actualizado.getId());
        }
    }
}
