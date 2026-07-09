package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.OrigenUsuario;
import com.escuelaing.usuarios.domain.model.Perfil;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void buscarOCrear_publicaUsuarioCreadoSoloUnaVez_alLlamarDosVeces() {
        // Primera invocación: no existe -> se crea.
        when(usuarioRepository.buscarPorEmail(EMAIL))
                .thenReturn(Optional.empty());
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUseCase.ResultadoFindOrCreate primera = usuarioService.buscarOCrear(EMAIL, NOMBRE, null);
        assertThat(primera.creado()).isTrue();

        // Segunda invocación: ya existe -> NO se vuelve a publicar usuario.creado.
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(primera.usuario()));
        UsuarioUseCase.ResultadoFindOrCreate segunda = usuarioService.buscarOCrear(EMAIL, NOMBRE, null);
        assertThat(segunda.creado()).isFalse();

        verify(eventPublisher, times(1))
                .publicarUsuarioCreado(any(), eq(EMAIL), eq(NOMBRE), any());
    }

    @Test
    void buscarOCrear_actualizaMicrosoftIdSoloSiEstabaAusente() {
        Usuario existenteSinMsId = Usuario.crearNuevo(EMAIL, NOMBRE, null);
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(existenteSinMsId));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioUseCase.ResultadoFindOrCreate resultado = usuarioService.buscarOCrear(EMAIL, NOMBRE, "ms-456");

        assertThat(resultado.creado()).isFalse();
        assertThat(resultado.usuario().getMicrosoftId()).isEqualTo("ms-456");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(captor.capture());
        assertThat(captor.getValue().getMicrosoftId()).isEqualTo("ms-456");
    }

    @Test
    void buscarOCrear_origenEsOtpCuandoNoLlegaMicrosoftId() {
        when(usuarioRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.buscarOCrear(EMAIL, NOMBRE, null);

        verify(eventPublisher).publicarUsuarioCreado(any(), eq(EMAIL), eq(NOMBRE), eq(OrigenUsuario.OTP));
    }
}
