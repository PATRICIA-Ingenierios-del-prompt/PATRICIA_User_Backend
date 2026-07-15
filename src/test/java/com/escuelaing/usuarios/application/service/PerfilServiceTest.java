package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.InteresInvalidoException;
import com.escuelaing.usuarios.domain.exception.PerfilNoEncontradoException;
import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Genero;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.outbound.FotoAlbumStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.FotoPerfilStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock private PerfilRepositoryPort perfilRepository;
    @Mock private UsuarioEventPublisherPort eventPublisher;
    @Mock private FotoPerfilStoragePort fotoPerfilStorage;
    @Mock private FotoAlbumStoragePort fotoAlbumStorage;

    private PerfilService perfilService;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        perfilService = new PerfilService(perfilRepository, eventPublisher,
                fotoPerfilStorage, fotoAlbumStorage);
        usuarioId = UUID.randomUUID();
    }

    @Test
    void obtenerPerfil_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.obtenerPerfil(usuarioId))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void obtenerPerfil_onboardingIncompleto_lanzaPerfilNoEncontradoException() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        // Por defecto, onboardingCompleto es false en perfil recién creado
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        assertThatThrownBy(() -> perfilService.obtenerPerfil(usuarioId))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void obtenerPerfil_onboardingCompleto_retornaPerfil() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        perfil.completarOnboarding("Nombre", "Apellidos", "Sistemas", null, 5,
                LocalDate.of(2000, 1, 1), Genero.MASCULINO, "url-foto", List.of("Gimnasio"));
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        Perfil resultado = perfilService.obtenerPerfil(usuarioId);

        assertThat(resultado).isEqualTo(perfil);
    }

    @Test
    void actualizarPerfil_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                perfilService.actualizarPerfil(usuarioId, "Bio", "Sistemas", 5, List.of("Gimnasio"), Disponibilidad.OCUPADO))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void actualizarPerfil_publicaPerfilActualizado_cuandoHayCambios() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        perfilService.actualizarPerfil(usuarioId, "Nueva bio", "Ingeniería de Sistemas", 5,
                List.of("Gimnasio"), Disponibilidad.OCUPADO);

        verify(eventPublisher, times(1)).publicarPerfilActualizado(eq(usuarioId), anyList());
    }

    @Test
    void actualizarPerfil_noPublicaNada_cuandoNoHayCambios() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        perfilService.actualizarPerfil(usuarioId, null, null, null, List.of(), Disponibilidad.DISPONIBLE);

        verify(eventPublisher, never()).publicarPerfilActualizado(any(), anyList());
        verify(perfilRepository, never()).guardar(any());
    }

    @Test
    void actualizarPerfil_lanzaInteresInvalido_cuandoInteresNoPerteneceAlCatalogo() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        assertThatThrownBy(() ->
                perfilService.actualizarPerfil(usuarioId, null, null, null,
                        List.of("Interés que no existe"), null))
                .isInstanceOf(InteresInvalidoException.class);

        verify(perfilRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarPerfilActualizado(any(), anyList());
    }

    @Test
    void obtenerDisponibilidad_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.obtenerDisponibilidad(usuarioId))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void obtenerDisponibilidad_perfilExiste_retornaDisponibilidad() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        Disponibilidad resultado = perfilService.obtenerDisponibilidad(usuarioId);

        assertThat(resultado).isEqualTo(Disponibilidad.DISPONIBLE);
    }

    @Test
    void obtenerIntereses_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.obtenerIntereses(usuarioId))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void obtenerIntereses_perfilExiste_retornaIntereses() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        perfil.actualizarIntereses(List.of("Gimnasio"));
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        List<String> resultado = perfilService.obtenerIntereses(usuarioId);

        assertThat(resultado).containsExactly("Gimnasio");
    }

    @Test
    void actualizarIntereses_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.actualizarIntereses(usuarioId, List.of("Gimnasio")))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void actualizarIntereses_sinCambios_retornaMismosInteresesSinGuardar() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        perfil.actualizarIntereses(List.of("Gimnasio"));
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        List<String> resultado = perfilService.actualizarIntereses(usuarioId, List.of("Gimnasio"));

        assertThat(resultado).containsExactly("Gimnasio");
        verify(perfilRepository, never()).guardar(any());
    }

    @Test
    void actualizarIntereses_publicaInteresesActualizados_cuandoCambianYSonValidos() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        List<String> nuevos = List.of("Gimnasio", "Fotografía");
        List<String> resultado = perfilService.actualizarIntereses(usuarioId, nuevos);

        assertThat(resultado).containsExactlyElementsOf(nuevos);
        verify(eventPublisher).publicarInteresesActualizados(usuarioId, nuevos);
        verify(eventPublisher).publicarPerfilActualizado(eq(usuarioId), eq(List.of("intereses")));
    }

    @Test
    void actualizarIntereses_lanzaInteresInvalido_cuandoNoPerteneceAlCatalogo() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        assertThatThrownBy(() -> perfilService.actualizarIntereses(usuarioId, List.of("No existe")))
                .isInstanceOf(InteresInvalidoException.class);
    }

    @Test
    void completarOnboarding_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.completarOnboarding(usuarioId, "Carlos", "Perez", "Sistemas",
                null, 4, LocalDate.of(2000, 1, 1), Genero.MASCULINO, null, List.of("Gimnasio")))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void completarOnboarding_conFotoValida_subeFotoYCompleta() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(fotoPerfilStorage.subirFotoPerfil(usuarioId, "data-foto")).thenReturn("http://s3/foto.jpg");
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = perfilService.completarOnboarding(usuarioId, "Carlos", "Perez", "Sistemas",
                null, 4, LocalDate.of(2000, 1, 1), Genero.MASCULINO, "data-foto", List.of("Gimnasio"));

        assertThat(resultado.getUrlFotoPerfil()).isEqualTo("http://s3/foto.jpg");
        assertThat(resultado.isOnboardingCompleto()).isTrue();
        verify(fotoPerfilStorage, times(1)).subirFotoPerfil(usuarioId, "data-foto");
        verify(perfilRepository, times(1)).guardar(any());
    }

    @Test
    void completarOnboarding_sinFoto_completaSinSubir() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = perfilService.completarOnboarding(usuarioId, "Carlos", "Perez", "Sistemas",
                null, 4, LocalDate.of(2000, 1, 1), Genero.MASCULINO, null, List.of("Gimnasio"));

        assertThat(resultado.getUrlFotoPerfil()).isNull();
        assertThat(resultado.isOnboardingCompleto()).isTrue();
        verify(fotoPerfilStorage, never()).subirFotoPerfil(any(), any());
    }

    // ── buscarUsuarios ───────────────────────────────────────────────────────

    @Test
    void buscarUsuarios_conQueryEnBlanco_devuelveListaVaciaSinConsultarRepositorio() {
        List<Perfil> resultado = perfilService.buscarUsuarios("   ", usuarioId, 20);
        assertThat(resultado).isEmpty();
        verify(perfilRepository, never()).buscarPorNombreOCarrera(any(), any(), anyInt());
    }

    @Test
    void buscarUsuarios_conQueryNula_devuelveListaVacia() {
        assertThat(perfilService.buscarUsuarios(null, usuarioId, 20)).isEmpty();
    }

    @Test
    void buscarUsuarios_conQueryValida_delegaAlRepositorioConTextoRecortado() {
        Perfil encontrado = Perfil.crearVacio(UUID.randomUUID());
        when(perfilRepository.buscarPorNombreOCarrera("sistemas", usuarioId, 20))
                .thenReturn(List.of(encontrado));

        List<Perfil> resultado = perfilService.buscarUsuarios("  sistemas  ", usuarioId, 20);

        assertThat(resultado).containsExactly(encontrado);
        verify(perfilRepository).buscarPorNombreOCarrera("sistemas", usuarioId, 20);
    }

    @Test
    void buscarUsuarios_conLimiteFueraDeRango_loAcotaEntre1y50() {
        when(perfilRepository.buscarPorNombreOCarrera(any(), any(), anyInt())).thenReturn(List.of());

        perfilService.buscarUsuarios("ana", usuarioId, 500);
        verify(perfilRepository).buscarPorNombreOCarrera("ana", usuarioId, 50);

        perfilService.buscarUsuarios("ana", usuarioId, -3);
        verify(perfilRepository).buscarPorNombreOCarrera("ana", usuarioId, 1);
    }

    // ── actualizarFotoPerfil (bytes) ──────────────────────────────────────────

    @Test
    void actualizarFotoPerfil_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.actualizarFotoPerfil(
                usuarioId, new byte[]{1, 2}, "image/jpeg"))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void actualizarFotoPerfil_mimeNoSoportado_lanzaDominioInvalido() {
        assertThatThrownBy(() -> perfilService.actualizarFotoPerfil(
                usuarioId, new byte[]{1}, "image/gif"))
                .isInstanceOf(com.escuelaing.usuarios.domain.exception.DominioInvalidoException.class);
    }

    @Test
    void actualizarFotoPerfil_valido_subeYPersistUrlYPublicaEvento() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(fotoAlbumStorage.subirFotoAlbum(any(), any(), any())).thenReturn("https://s3/nueva.jpg");
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = perfilService.actualizarFotoPerfil(
                usuarioId, new byte[]{(byte) 0xFF, (byte) 0xD8}, "image/jpeg");

        assertThat(resultado.getUrlFotoPerfil()).isEqualTo("https://s3/nueva.jpg");
        verify(fotoAlbumStorage).subirFotoAlbum(eq(usuarioId), any(), eq("image/jpeg"));
        verify(perfilRepository).guardar(perfil);
        verify(eventPublisher).publicarPerfilActualizado(usuarioId, List.of("urlFotoPerfil"));
    }

    // ── actualizarFotoPerfilDesdeDataUrl ──────────────────────────────────────

    @Test
    void actualizarFotoPerfilDesdeDataUrl_formatoInvalido_lanzaDominioInvalido() {
        assertThatThrownBy(() -> perfilService.actualizarFotoPerfilDesdeDataUrl(
                usuarioId, "no-es-data-url"))
                .isInstanceOf(com.escuelaing.usuarios.domain.exception.DominioInvalidoException.class);
    }

    @Test
    void actualizarFotoPerfilDesdeDataUrl_base64Invalido_lanzaDominioInvalido() {
        assertThatThrownBy(() -> perfilService.actualizarFotoPerfilDesdeDataUrl(
                usuarioId, "data:image/jpeg;base64,!!!no-valido!!!"))
                .isInstanceOf(com.escuelaing.usuarios.domain.exception.DominioInvalidoException.class);
    }

    @Test
    void actualizarFotoPerfilDesdeDataUrl_valido_subeYPersiste() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(fotoAlbumStorage.subirFotoAlbum(any(), any(), any())).thenReturn("https://s3/foto.png");
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = perfilService.actualizarFotoPerfilDesdeDataUrl(
                usuarioId, "data:image/png;base64,AAAA");

        assertThat(resultado.getUrlFotoPerfil()).isEqualTo("https://s3/foto.png");
        verify(fotoAlbumStorage).subirFotoAlbum(eq(usuarioId), any(byte[].class), eq("image/png"));
    }

    // ── marcarPersonaEnFotoPerfil ─────────────────────────────────────────────

    @Test
    void marcarPersonaEnFotoPerfil_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.marcarPersonaEnFotoPerfil(usuarioId))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void marcarPersonaEnFotoPerfil_cambiaDefalseATrue_persisteYPublicaEvento() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        assertThat(perfil.isTienePersonaEnFoto()).isFalse();

        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = perfilService.marcarPersonaEnFotoPerfil(usuarioId);

        assertThat(resultado.isTienePersonaEnFoto()).isTrue();
        verify(perfilRepository).guardar(perfil);
        verify(eventPublisher).publicarPersonaDetectadaEnFoto(eq(usuarioId), any());
    }

    @Test
    void marcarPersonaEnFotoPerfil_yaEstaMarcada_noVuelveAPublicarEvento() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        perfil.marcarPersonaDetectadaEnFoto();
        assertThat(perfil.isTienePersonaEnFoto()).isTrue();

        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        perfilService.marcarPersonaEnFotoPerfil(usuarioId);

        verify(eventPublisher, never()).publicarPersonaDetectadaEnFoto(any(), any());
        verify(perfilRepository).guardar(perfil);
    }

    // ── actualizarFranjasDisponibilidad ───────────────────────────────────────

    @Test
    void actualizarFranjasDisponibilidad_perfilNoExiste_lanzaPerfilNoEncontradoException() {
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> perfilService.actualizarFranjasDisponibilidad(
                usuarioId, List.of()))
                .isInstanceOf(PerfilNoEncontradoException.class);
    }

    @Test
    void actualizarFranjasDisponibilidad_listaVacia_limpiaTodas() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Perfil resultado = perfilService.actualizarFranjasDisponibilidad(usuarioId, List.of());

        assertThat(resultado.getFranjasDisponibilidad()).isEmpty();
        verify(perfilRepository).guardar(perfil);
        verify(eventPublisher).publicarPerfilActualizado(usuarioId, List.of("franjasDisponibilidad"));
    }

    @Test
    void actualizarFranjasDisponibilidad_conFranjas_persisteYPublicaEvento() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        UUID perfilId = perfil.getId();
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        List<com.escuelaing.usuarios.domain.model.FranjaHoraria> franjas = List.of(
                com.escuelaing.usuarios.domain.model.FranjaHoraria.crear(
                        perfilId, java.time.DayOfWeek.MONDAY,
                        java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0)),
                com.escuelaing.usuarios.domain.model.FranjaHoraria.crear(
                        perfilId, java.time.DayOfWeek.WEDNESDAY,
                        java.time.LocalTime.of(14, 0), java.time.LocalTime.of(16, 0))
        );

        Perfil resultado = perfilService.actualizarFranjasDisponibilidad(usuarioId, franjas);

        assertThat(resultado.getFranjasDisponibilidad()).hasSize(2);
        verify(eventPublisher).publicarPerfilActualizado(usuarioId, List.of("franjasDisponibilidad"));
    }

    @Test
    void actualizarFranjasDisponibilidad_franjaConHoraInicioMayorAFin_lanzaDominioInvalido() {
        Perfil perfil = Perfil.crearVacio(usuarioId);

        assertThatThrownBy(() -> perfilService.actualizarFranjasDisponibilidad(
                usuarioId, List.of(
                        com.escuelaing.usuarios.domain.model.FranjaHoraria.crear(
                                perfil.getId(), java.time.DayOfWeek.FRIDAY,
                                java.time.LocalTime.of(18, 0), java.time.LocalTime.of(8, 0)))))
                .isInstanceOf(com.escuelaing.usuarios.domain.exception.DominioInvalidoException.class);
    }
}
