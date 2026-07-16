package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.CredencialesInvalidasException;
import com.escuelaing.usuarios.domain.model.CredencialJurado;
import com.escuelaing.usuarios.domain.model.OrigenUsuario;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.JuradoUseCase;
import com.escuelaing.usuarios.domain.port.outbound.CredencialJuradoRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.PasswordVerifierPort;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementación del caso de uso JuradoUseCase.
 *
 * Regla clave: las credenciales (correo + hash) se cargan manualmente por un
 * admin en credenciales_jurado; este servicio NUNCA las crea. Lo único que
 * hace en el primer login exitoso es dar de alta el Usuario/Perfil (igual
 * que find-or-create para estudiantes) y enlazarlo con la credencial.
 */
@Service
@Transactional
public class JuradoService implements JuradoUseCase {

    private final CredencialJuradoRepositoryPort credencialRepository;
    private final PasswordVerifierPort passwordVerifier;
    private final UsuarioRepositoryPort usuarioRepository;
    private final PerfilRepositoryPort perfilRepository;
    private final UsuarioEventPublisherPort eventPublisher;

    public JuradoService(CredencialJuradoRepositoryPort credencialRepository,
                          PasswordVerifierPort passwordVerifier,
                          UsuarioRepositoryPort usuarioRepository,
                          PerfilRepositoryPort perfilRepository,
                          UsuarioEventPublisherPort eventPublisher) {
        this.credencialRepository = credencialRepository;
        this.passwordVerifier = passwordVerifier;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Usuario autenticar(String email, String passwordPlano) {
        String normalizado = email == null ? null : email.trim().toLowerCase();

        CredencialJurado credencial = credencialRepository.buscarPorEmail(normalizado)
                .orElseThrow(CredencialesInvalidasException::new);

        if (passwordPlano == null || !passwordVerifier.coincide(passwordPlano, credencial.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        if (!credencial.primerIngreso()) {
            return usuarioRepository.buscarPorId(credencial.getUsuarioId())
                    .orElseGet(() -> crearUsuarioJurado(credencial));
        }

        return crearUsuarioJurado(credencial);
    }

    private Usuario crearUsuarioJurado(CredencialJurado credencial) {
        Usuario nuevoUsuario = Usuario.crearJurado(credencial.getEmail(), deriveName(credencial.getEmail()));
        Usuario guardado = usuarioRepository.guardar(nuevoUsuario);

        Perfil perfilVacio = Perfil.crearVacio(guardado.getId());
        perfilRepository.guardar(perfilVacio);

        credencial.enlazarUsuario(guardado.getId());
        credencialRepository.guardar(credencial);

        eventPublisher.publicarUsuarioCreado(guardado.getId(), guardado.getEmail(), guardado.getNombre(),
                OrigenUsuario.JURADO);

        return guardado;
    }

    private String deriveName(String email) {
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
