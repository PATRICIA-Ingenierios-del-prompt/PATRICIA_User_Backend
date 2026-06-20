package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.OrigenUsuario;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.model.RolPlataforma;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.domain.port.out.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.out.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.out.UsuarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Implementación del caso de uso UsuarioUseCase.
 *
 * Regla clave: find-or-create es idempotente.
 * 1. Busca por email.
 * 2. Si existe, lo devuelve (y actualiza microsoftId solo si estaba vacío).
 * 3. Si no existe, lo crea junto con un perfil vacío.
 * 4. Publica usuario.creado SOLO cuando realmente se crea.
 */
@Service
@Transactional
public class UsuarioService implements UsuarioUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PerfilRepositoryPort perfilRepository;
    private final UsuarioEventPublisherPort eventPublisher;

    public UsuarioService(UsuarioRepositoryPort usuarioRepository,
                           PerfilRepositoryPort perfilRepository,
                           UsuarioEventPublisherPort eventPublisher) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ResultadoFindOrCreate buscarOCrear(String email, String nombre, String microsoftId) {
        Optional<Usuario> existente = usuarioRepository.buscarPorEmail(email);

        if (existente.isPresent()) {
            Usuario usuario = existente.get();
            boolean actualizado = usuario.asignarMicrosoftIdSiAusente(microsoftId);
            if (actualizado) {
                usuario = usuarioRepository.guardar(usuario);
            }
            return new ResultadoFindOrCreate(usuario, false);
        }

        Usuario nuevoUsuario = Usuario.crearNuevo(email, nombre, microsoftId);
        Usuario guardado = usuarioRepository.guardar(nuevoUsuario);

        Perfil perfilVacio = Perfil.crearVacio(guardado.getId());
        perfilRepository.guardar(perfilVacio);

        OrigenUsuario origen = (microsoftId != null && !microsoftId.isBlank())
                ? OrigenUsuario.MICROSOFT
                : OrigenUsuario.OTP;

        eventPublisher.publicarUsuarioCreado(guardado.getId(), guardado.getEmail(), guardado.getNombre(), origen);

        return new ResultadoFindOrCreate(guardado, true);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.buscarPorEmail(email);
    }

    @Override
    public Usuario cambiarEstado(UUID id, EstadoUsuario nuevoEstado) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        usuario.cambiarEstado(nuevoEstado);
        Usuario actualizado = usuarioRepository.guardar(usuario);

        if (nuevoEstado == EstadoUsuario.BANNED) {
            eventPublisher.publicarUsuarioBaneado(actualizado.getId());
        } else {
            eventPublisher.publicarUsuarioActualizado(actualizado.getId(), java.util.List.of("estado"));
        }

        return actualizado;
    }

    @Override
    public Usuario actualizarRoles(UUID id, Set<RolPlataforma> roles) {
        Usuario usuario = usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        usuario.asignarRoles(roles);
        Usuario actualizado = usuarioRepository.guardar(usuario);

        eventPublisher.publicarUsuarioActualizado(actualizado.getId(), java.util.List.of("roles"));

        return actualizado;
    }
}
