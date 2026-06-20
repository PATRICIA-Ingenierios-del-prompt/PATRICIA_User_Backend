package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.EstadoUsuario;
import com.escuelaing.usuarios.domain.model.MotivoSuspension;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.out.AuthServicePort;
import com.escuelaing.usuarios.domain.port.out.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.out.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el caso de uso ReporteUseCase (implementado por ReporteService),
 * que es invocado por ReporteConsumer al recibir reporte.emitido desde
 * el exchange patricia.parches.
 */
@ExtendWith(MockitoExtension.class)
class ReporteConsumerTest {

    private static final int MAX_REPORTES = 5;

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
        reporteService = new ReporteService(usuarioRepository, eventPublisher, authServicePort, MAX_REPORTES);
        usuarioId = UUID.randomUUID();
    }

    private Usuario usuarioConReportes(int contadorReportes) throws Exception {
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);
        Field campo = Usuario.class.getDeclaredField("contadorReportes");
        campo.setAccessible(true);
        campo.set(usuario, contadorReportes);
        return usuario;
    }

    @Test
    void registrarReporte_alAlcanzarElUmbral_suspendeAlUsuario() throws Exception {
        Usuario usuario = usuarioConReportes(MAX_REPORTES - 1); // próximo reporte alcanza el umbral
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        reporteService.registrarReporte(usuarioId);

        assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.SUSPENDED);
        assertThat(usuario.getContadorReportes()).isEqualTo(MAX_REPORTES);
    }

    @Test
    void registrarReporte_alAlcanzarElUmbral_publicaUsuarioSuspendido() throws Exception {
        Usuario usuario = usuarioConReportes(MAX_REPORTES - 1);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        reporteService.registrarReporte(usuarioId);

        verify(eventPublisher, times(1))
                .publicarUsuarioSuspendido(usuarioId, MotivoSuspension.REPORTES, MAX_REPORTES);
    }

    @Test
    void registrarReporte_alAlcanzarElUmbral_invocaCierreDeSesionEnAuthService() throws Exception {
        Usuario usuario = usuarioConReportes(MAX_REPORTES - 1);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        reporteService.registrarReporte(usuarioId);

        verify(authServicePort, times(1)).cerrarSesion(usuarioId);
    }

    @Test
    void registrarReporte_antesDeAlcanzarElUmbral_noSuspendeNiPublicaNiCierraSesion() throws Exception {
        Usuario usuario = usuarioConReportes(1);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        reporteService.registrarReporte(usuarioId);

        assertThat(usuario.getEstado()).isEqualTo(EstadoUsuario.ACTIVE);
        verify(eventPublisher, never()).publicarUsuarioSuspendido(any(), any(), anyInt());
        verify(authServicePort, never()).cerrarSesion(any());
    }
}
