package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.outbound.AuthServicePort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private UsuarioEventPublisherPort eventPublisher;

    @Mock
    private AuthServicePort authServicePort;

    private ReporteService reporteService;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        reporteService = new ReporteService(usuarioRepository, eventPublisher, authServicePort, 5);
        usuarioId = UUID.randomUUID();
    }

    @Test
    void registrarReporte_usuarioNoExiste_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reporteService.registrarReporte(usuarioId))
                .isInstanceOf(UsuarioNoEncontradoException.class);

        verify(usuarioRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarUsuarioSuspendido(any(), any(), anyInt());
        verify(authServicePort, never()).cerrarSesion(any());
    }

    @Test
    void registrarReporte_incrementaReportesSinSuspender_cuandoNoAlcanzaUmbral() {
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test User", null);
        // El usuario inicia con 0 reportes y estado ACTIVE
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        reporteService.registrarReporte(usuarioId);

        assertThat(usuario.getContadorReportes()).isEqualTo(1);
        assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.ACTIVE);

        verify(usuarioRepository, times(1)).guardar(usuario);
        verify(eventPublisher, never()).publicarUsuarioSuspendido(any(), any(), anyInt());
        verify(authServicePort, never()).cerrarSesion(any());
    }

    @Test
    void registrarReporte_suspendeUsuarioYPublicaEventos_cuandoAlcanzaUmbral() {
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test User", null);
        // Forzamos al usuario a tener 4 reportes
        for (int i = 0; i < 4; i++) {
            usuario.incrementarReportes();
        }
        assertThat(usuario.getContadorReportes()).isEqualTo(4);

        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        reporteService.registrarReporte(usuarioId);

        assertThat(usuario.getContadorReportes()).isEqualTo(5);
        assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.SUSPENDED);

        verify(usuarioRepository, times(1)).guardar(usuario);
        verify(eventPublisher, times(1))
                .publicarUsuarioSuspendido(usuario.getId(), MotivoSuspension.REPORTES, 5);
        verify(authServicePort, times(1)).cerrarSesion(usuario.getId());
    }
}
