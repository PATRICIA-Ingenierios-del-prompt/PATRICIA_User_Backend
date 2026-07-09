package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifica el caso de uso SesionUseCase (implementado por SesionService),
 * invocado por AuthSesionConsumer al recibir sesion.iniciada / auth.fallido
 * desde el exchange patricia.auth.
 */
@ExtendWith(MockitoExtension.class)
class AuthSesionConsumerTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    private SesionService sesionService;

    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        sesionService = new SesionService(usuarioRepository);
        usuarioId = UUID.randomUUID();
    }

    @Test
    void registrarSesionIniciada_actualizaUltimoAcceso() {
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test", null);
        assertThat(usuario.getUltimoAcceso()).isNull();

        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        sesionService.registrarSesionIniciada(usuarioId);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).guardar(captor.capture());
        assertThat(captor.getValue().getUltimoAcceso()).isNotNull();
    }

    @Test
    void registrarSesionIniciada_usuarioInexistente_lanzaExcepcion() {
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sesionService.registrarSesionIniciada(usuarioId))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void registrarAuthFallido_noModificaElUsuario() {
        sesionService.registrarAuthFallido(usuarioId, "CREDENCIALES_INVALIDAS");

        verify(usuarioRepository, org.mockito.Mockito.never()).guardar(any());
        verify(usuarioRepository, org.mockito.Mockito.never()).buscarPorId(any());
    }
}
