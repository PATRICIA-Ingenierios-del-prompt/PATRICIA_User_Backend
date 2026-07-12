package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.EstadoUsuarioInvalidoException;
import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.OrigenUsuario;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.model.RolPlataforma;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.in.UsuarioUseCase;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private PerfilRepositoryPort perfilRepository;
    @Mock private UsuarioEventPublisherPort eventPublisher;

    private UsuarioService usuarioService;

    private static final String EMAIL = "juan.perez@mail.escuelaing.edu.co";
    private static final String NOMBRE = "Juan Pérez";

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, perfilRepository, eventPublisher);
    }

    // ── buscarOCrear ──────────────────────────────────────────────────────────

    @Test
    void buscarOCrear_cuandoUsuarioNoExiste_loCreaYPublicaEvento() {
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUseCase.ResultadoFindOrCreate resultado = usuarioService.buscarOCrear(EMAIL, NOMBRE, "ms-123");

        assertThat(resultado.creado()).isTrue();
        assertThat(resultado.usuario().getEmail()).isEqualTo(EMAIL);
        verify(eventPublisher).publicarUsuarioCreado(any(), eq(EMAIL), eq(NOMBRE), eq(OrigenUsuario.MICROSOFT));
    }

    @Test
    void buscarOCrear_cuandoUsuarioYaExiste_esIdempotenteYNoPublicaEvento() {
        Usuario existente = Usuario.crearNuevo(EMAIL, NOMBRE, "ms-123");
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(existente));

        UsuarioUseCase.ResultadoFindOrCreate resultado = usuarioService.buscarOCrear(EMAIL, NOMBRE, "ms-123");

        assertThat(resultado.creado()).isFalse();
        verify(usuarioRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarUsuarioCreado(any(), any(), any(), any());
    }

    // ── buscarPorId ───────────────────────────────────────────────────────────

    @Test
    void buscarPorId_noExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(id))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void buscarPorId_existe_retornaUsuario() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));

        assertThat(usuarioService.buscarPorId(id)).isEqualTo(usuario);
    }

    // ── buscarPorEmail ────────────────────────────────────────────────────────

    @Test
    void buscarPorEmail_retornaOpcional() {
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));

        assertThat(usuarioService.buscarPorEmail(EMAIL)).contains(usuario);
    }

    // ── cambiarEstado ─────────────────────────────────────────────────────────

    @Test
    void cambiarEstado_aBanned_publicaUsuarioBaneado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.cambiarEstado(id, EstadoUsuario.BANNED);

        assertThat(resultado.getEstado()).isEqualTo(EstadoUsuario.BANNED);
        verify(eventPublisher).publicarUsuarioBaneado(usuario.getId());
        verify(eventPublisher, never()).publicarUsuarioActualizado(any(), any());
    }

    @Test
    void cambiarEstado_aSuspended_publicaUsuarioActualizado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.cambiarEstado(id, EstadoUsuario.SUSPENDED);

        verify(eventPublisher).publicarUsuarioActualizado(usuario.getId(), List.of("estado"));
        verify(eventPublisher, never()).publicarUsuarioBaneado(any());
    }

    // ── actualizarRoles ───────────────────────────────────────────────────────

    @Test
    void actualizarRoles_noExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.actualizarRoles(id, Set.of(RolPlataforma.ADMIN)))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void actualizarRoles_guardaYPublicaActualizado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.actualizarRoles(id, Set.of(RolPlataforma.ADMIN));

        assertThat(resultado.getRoles()).containsExactly(RolPlataforma.ADMIN);
        verify(eventPublisher).publicarUsuarioActualizado(usuario.getId(), List.of("roles"));
    }

    // ── cerrarCuenta ──────────────────────────────────────────────────────────

    @Test
    void cerrarCuenta_noExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cerrarCuenta(id))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void cerrarCuenta_activo_cambiaEstadoAPendingDeletionYGuarda() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.cerrarCuenta(id);

        assertThat(resultado.getEstado()).isEqualTo(EstadoUsuario.PENDING_DELETION);
        assertThat(resultado.getFechaSolicitudEliminacion()).isNotNull();
        verify(usuarioRepository).guardar(usuario);
        verify(eventPublisher).publicarUsuarioActualizado(usuario.getId(),
                List.of("estado", "fechaSolicitudEliminacion"));
    }

    @Test
    void cerrarCuenta_yaPendingDeletion_lanzaEstadoInvalidoException() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        usuario.solicitarEliminacion(); // ya está PENDING_DELETION
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.cerrarCuenta(id))
                .isInstanceOf(EstadoUsuarioInvalidoException.class);
    }

    // ── cancelarCierreCuenta ─────────────────────────────────────────────────

    @Test
    void cancelarCierreCuenta_noExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cancelarCierreCuenta(id))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void cancelarCierreCuenta_pendingDeletion_restauraAActive() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        usuario.solicitarEliminacion();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.cancelarCierreCuenta(id);

        assertThat(resultado.getEstado()).isEqualTo(EstadoUsuario.ACTIVE);
        assertThat(resultado.getFechaSolicitudEliminacion()).isNull();
        verify(usuarioRepository).guardar(usuario);
    }

    @Test
    void cancelarCierreCuenta_noEstaPendiente_lanzaEstadoInvalidoException() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null); // estado ACTIVE
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> usuarioService.cancelarCierreCuenta(id))
                .isInstanceOf(EstadoUsuarioInvalidoException.class);
    }
}
