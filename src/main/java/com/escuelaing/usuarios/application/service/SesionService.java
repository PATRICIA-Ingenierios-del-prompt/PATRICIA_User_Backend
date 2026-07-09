package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.SesionUseCase;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementación del caso de uso SesionUseCase: procesa eventos de sesión
 * provenientes de auth-service (sesion.iniciada, auth.fallido, sesion.cerrada).
 */
@Service
@Transactional
public class SesionService implements SesionUseCase {

    private static final Logger log = LoggerFactory.getLogger(SesionService.class);

    private final UsuarioRepositoryPort usuarioRepository;

    public SesionService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void registrarSesionIniciada(UUID usuarioId) {
        Usuario usuario = usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        usuario.registrarAcceso();
        usuarioRepository.guardar(usuario);
    }

    @Override
    public void registrarAuthFallido(UUID usuarioId, String motivo) {
        // Solo auditoría: no se modifica el estado del usuario.
        log.info("auth.fallido recibido para usuario {}: motivo={}", usuarioId, motivo);
    }

    @Override
    public void registrarSesionCerrada(UUID usuarioId) {
        // Solo auditoría.
        log.info("sesion.cerrada recibido para usuario {}", usuarioId);
    }
}
