package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Usuario;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SesionServiceTest {

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
    void registrarSesionIniciada_usuarioNoExiste_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sesionService.registrarSesionIniciada(usuarioId))
                .isInstanceOf(UsuarioNoEncontradoException.class);

        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void registrarSesionIniciada_usuarioExiste_registraAccesoYGuarda() {
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test User", null);
        assertThat(usuario.getUltimoAcceso()).isNull();

        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.guardar(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        sesionService.registrarSesionIniciada(usuarioId);

        assertThat(usuario.getUltimoAcceso()).isNotNull();
        verify(usuarioRepository, times(1)).guardar(usuario);
    }

    @Test
    void registrarAuthFallido_noLanzaExcepcion() {
        // Debiese ejecutarse correctamente registrando logs.
        sesionService.registrarAuthFallido(usuarioId, "CREDENCIALES_INVALIDAS");
    }

    @Test
    void registrarSesionCerrada_noLanzaExcepcion() {
        // Debiese ejecutarse correctamente registrando logs.
        sesionService.registrarSesionCerrada(usuarioId);
    }
}
