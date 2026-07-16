package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.CredencialesInvalidasException;
import com.escuelaing.usuarios.domain.model.CredencialJurado;
import com.escuelaing.usuarios.domain.model.OrigenUsuario;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.outbound.CredencialJuradoRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.PasswordVerifierPort;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JuradoServiceTest {

    @Mock private CredencialJuradoRepositoryPort credencialRepository;
    @Mock private PasswordVerifierPort passwordVerifier;
    @Mock private UsuarioRepositoryPort usuarioRepository;
    @Mock private PerfilRepositoryPort perfilRepository;
    @Mock private UsuarioEventPublisherPort eventPublisher;

    private JuradoService juradoService;

    private static final String EMAIL = "jurado@ejemplo.com";
    private static final String PASSWORD = "secret";

    @BeforeEach
    void setUp() {
        juradoService = new JuradoService(
                credencialRepository, passwordVerifier, usuarioRepository, perfilRepository, eventPublisher
        );
    }

    private CredencialJurado credencialSinUsuario() {
        return CredencialJurado.reconstruir(
                UUID.randomUUID(), EMAIL, "hash", null, Instant.now(), Instant.now()
        );
    }

    private CredencialJurado credencialConUsuario(UUID usuarioId) {
        return CredencialJurado.reconstruir(
                UUID.randomUUID(), EMAIL, "hash", usuarioId, Instant.now(), Instant.now()
        );
    }

    // ── autenticar: correo/contraseña inválidos ────────────────────────────────

    @Test
    void autenticar_lanzaCredencialesInvalidas_cuandoElCorreoNoExiste() {
        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> juradoService.autenticar(EMAIL, PASSWORD))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void autenticar_lanzaCredencialesInvalidas_cuandoLaContrasenaNoCoincide() {
        CredencialJurado credencial = credencialSinUsuario();
        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));
        when(passwordVerifier.coincide(PASSWORD, "hash")).thenReturn(false);

        assertThatThrownBy(() -> juradoService.autenticar(EMAIL, PASSWORD))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    void autenticar_lanzaCredencialesInvalidas_cuandoLaContrasenaEsNula() {
        CredencialJurado credencial = credencialSinUsuario();
        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));

        assertThatThrownBy(() -> juradoService.autenticar(EMAIL, null))
                .isInstanceOf(CredencialesInvalidasException.class);

        verify(passwordVerifier, never()).coincide(any(), any());
    }

    @Test
    void autenticar_normalizaElCorreoAMinusculasYSinEspacios() {
        CredencialJurado credencial = credencialSinUsuario();
        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));
        when(passwordVerifier.coincide(PASSWORD, "hash")).thenReturn(true);
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(credencialRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        juradoService.autenticar("  Jurado@Ejemplo.com  ", PASSWORD);

        verify(credencialRepository).buscarPorEmail(EMAIL);
    }

    // ── autenticar: primer ingreso (crea Usuario/Perfil) ───────────────────────

    @Test
    void autenticar_primerIngreso_creaUsuarioYPerfilYEnlazaCredencial() {
        CredencialJurado credencial = credencialSinUsuario();
        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));
        when(passwordVerifier.coincide(PASSWORD, "hash")).thenReturn(true);
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(credencialRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = juradoService.autenticar(EMAIL, PASSWORD);

        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        verify(usuarioRepository).guardar(any());
        verify(perfilRepository).guardar(any());
        verify(credencialRepository).guardar(credencial);
        verify(eventPublisher).publicarUsuarioCreado(eq(resultado.getId()), eq(EMAIL), any(), eq(OrigenUsuario.JURADO));
    }

    // ── autenticar: ingresos posteriores (usuario ya enlazado) ─────────────────

    @Test
    void autenticar_ingresoPosterior_retornaUsuarioExistente_sinCrearNiPublicarEvento() {
        UUID usuarioId = UUID.randomUUID();
        CredencialJurado credencial = credencialConUsuario(usuarioId);
        Usuario existente = Usuario.crearJurado(EMAIL, "jurado");

        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));
        when(passwordVerifier.coincide(PASSWORD, "hash")).thenReturn(true);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(existente));

        Usuario resultado = juradoService.autenticar(EMAIL, PASSWORD);

        assertThat(resultado).isEqualTo(existente);
        verify(usuarioRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarUsuarioCreado(any(), any(), any(), any());
    }

    @Test
    void autenticar_ingresoPosterior_recreaUsuario_cuandoElUsuarioEnlazadoYaNoExiste() {
        UUID usuarioId = UUID.randomUUID();
        CredencialJurado credencial = credencialConUsuario(usuarioId);

        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));
        when(passwordVerifier.coincide(PASSWORD, "hash")).thenReturn(true);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(credencialRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = juradoService.autenticar(EMAIL, PASSWORD);

        assertThat(resultado.getEmail()).isEqualTo(EMAIL);
        verify(usuarioRepository).guardar(any());
        verify(eventPublisher).publicarUsuarioCreado(any(), eq(EMAIL), any(), eq(OrigenUsuario.JURADO));
    }

    // ── autenticar: nombre derivado del correo ──────────────────────────────────

    @Test
    void autenticar_derivaElNombreDeLaParteLocalDelCorreo() {
        CredencialJurado credencial = credencialSinUsuario();
        when(credencialRepository.buscarPorEmail(EMAIL)).thenReturn(Optional.of(credencial));
        when(passwordVerifier.coincide(PASSWORD, "hash")).thenReturn(true);
        when(usuarioRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(credencialRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = juradoService.autenticar(EMAIL, PASSWORD);

        assertThat(resultado.getNombre()).isEqualTo("jurado");
    }
}
