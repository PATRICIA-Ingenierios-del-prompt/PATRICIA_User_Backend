package com.escuelaing.usuarios.application.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private PerfilRepositoryPort perfilRepository;

    @Mock
    private UsuarioEventPublisherPort eventPublisher;

    private UsuarioService usuarioService;

    private static final String EMAIL = "juan.perez@mail.escuelaing.edu.co";
    private static final String NOMBRE = "Juan Pérez";

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, perfilRepository, eventPublisher);
    }

    @Test
    void buscarOCrear_cuandoUsuarioNoExiste_loCreaYPublicaEvento() {
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUseCase.ResultadoFindOrCreate resultado = usuarioService.buscarOCrear(EMAIL, NOMBRE, "ms-123");

        assertThat(resultado.creado()).isTrue();
        assertThat(resultado.usuario().getEmail()).isEqualTo(EMAIL);
        assertThat(resultado.usuario().getNombre()).isEqualTo(NOMBRE);

        verify(usuarioRepository, times(1)).guardar(any(Usuario.class));
        verify(perfilRepository, times(1)).guardar(any(Perfil.class));
        verify(eventPublisher, times(1))
                .publicarUsuarioCreado(any(), eq(EMAIL), eq(NOMBRE), eq(OrigenUsuario.MICROSOFT));
    }

    @Test
    void buscarOCrear_cuandoUsuarioYaExiste_esIdempotenteYNoPublicaEvento() {
        Usuario existente = Usuario.crearNuevo(EMAIL, NOMBRE, "ms-123");
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(existente));

        UsuarioUseCase.ResultadoFindOrCreate resultado = usuarioService.buscarOCrear(EMAIL, NOMBRE, "ms-123");

        assertThat(resultado.creado()).isFalse();
        assertThat(resultado.usuario().getId()).isEqualTo(existente.getId());

        verify(usuarioRepository, never()).guardar(any());
        verify(perfilRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarUsuarioCreado(any(), any(), any(), any());
    }

    @Test
    void buscarPorId_cuandoNoExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(id))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void buscarPorId_cuandoExiste_retornaUsuario() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));

        Usuario resultado = usuarioService.buscarPorId(id);

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    void buscarPorEmail_retornaUsuarioDeManeraOpcional() {
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail(EMAIL);

        assertThat(resultado).contains(usuario);

        when(usuarioRepository.buscarPorEmail("invalido")).thenReturn(Optional.empty());
        assertThat(usuarioService.buscarPorEmail("invalido")).isEmpty();
    }

    @Test
    void cambiarEstado_cuandoUsuarioNoExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.cambiarEstado(id, EstadoUsuario.ACTIVE))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void cambiarEstado_aBanned_publicaUsuarioBaneado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.cambiarEstado(id, EstadoUsuario.BANNED);

        assertThat(resultado.getEstado()).isEqualTo(EstadoUsuario.BANNED);
        verify(eventPublisher, times(1)).publicarUsuarioBaneado(usuario.getId());
        verify(eventPublisher, never()).publicarUsuarioActualizado(any(), any());
    }

    @Test
    void cambiarEstado_aSuspended_publicaUsuarioActualizado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.cambiarEstado(id, EstadoUsuario.SUSPENDED);

        assertThat(resultado.getEstado()).isEqualTo(EstadoUsuario.SUSPENDED);
        verify(eventPublisher, times(1)).publicarUsuarioActualizado(usuario.getId(), java.util.List.of("estado"));
        verify(eventPublisher, never()).publicarUsuarioBaneado(any());
    }

    @Test
    void actualizarRoles_cuandoUsuarioNoExiste_lanzaUsuarioNoEncontradoException() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.actualizarRoles(id, Set.of(RolPlataforma.ADMIN)))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void actualizarRoles_conUsuarioExistente_guardaYPublicaActualizado() {
        UUID id = UUID.randomUUID();
        Usuario usuario = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorId(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.actualizarRoles(id, Set.of(RolPlataforma.ADMIN));

        assertThat(resultado.getRoles()).containsExactly(RolPlataforma.ADMIN);
        verify(eventPublisher, times(1)).publicarUsuarioActualizado(usuario.getId(), java.util.List.of("roles"));
    }
}
