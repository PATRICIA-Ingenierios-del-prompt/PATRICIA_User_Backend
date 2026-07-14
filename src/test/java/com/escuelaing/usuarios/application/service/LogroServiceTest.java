package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.UsuarioNoEncontradoException;
import com.escuelaing.usuarios.domain.model.CategoriaActividad;
import com.escuelaing.usuarios.domain.model.LogroTipo;
import com.escuelaing.usuarios.domain.model.LogrosUsuario;
import com.escuelaing.usuarios.domain.model.Usuario;
import com.escuelaing.usuarios.domain.port.outbound.BienestarPort;
import com.escuelaing.usuarios.domain.port.outbound.LogroEventPublisherPort;
import com.escuelaing.usuarios.domain.port.outbound.LogroRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.LogroSenalRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogroServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @Mock
    private LogroRepositoryPort logroRepository;
    @Mock
    private LogroSenalRepositoryPort senalRepository;
    @Mock
    private BienestarPort bienestarPort;
    @Mock
    private LogroEventPublisherPort eventPublisher;

    private LogroService logroService;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        logroService = new LogroService(usuarioRepository, logroRepository, senalRepository, bienestarPort, eventPublisher);
        usuarioId = UUID.randomUUID();
    }

    @Test
    void obtenerLogros_usuarioNoExiste_lanzaUsuarioNoEncontradoException() {
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> logroService.obtenerLogros(usuarioId))
                .isInstanceOf(UsuarioNoEncontradoException.class);
    }

    @Test
    void obtenerLogros_devuelveCatalogoCompletoConEstadoYXpTotal() {
        Usuario usuario = Usuario.crearNuevo("test@mail.escuelaing.edu.co", "Test User", null);
        when(usuarioRepository.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(logroRepository.buscarDesbloqueadosConFecha(usuarioId))
                .thenReturn(Map.of(LogroTipo.MONA_CODER, java.time.Instant.now()));
        when(logroRepository.calcularXpTotal(usuarioId)).thenReturn(50);

        LogrosUsuario resultado = logroService.obtenerLogros(usuarioId);

        assertThat(resultado.xpTotal()).isEqualTo(50);
        assertThat(resultado.logros()).hasSize(LogroTipo.values().length);
        assertThat(resultado.logros().stream().filter(l -> l.tipo() == LogroTipo.MONA_CODER).findFirst())
                .get().extracting("desbloqueado").isEqualTo(true);
        assertThat(resultado.logros().stream().filter(l -> l.tipo() == LogroTipo.MONA_DJ).findFirst())
                .get().extracting("desbloqueado").isEqualTo(false);
    }

    @Test
    void procesarActividadParche_technology_otorgaMonaCoder() {
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), eq(LogroTipo.MONA_CODER), anyInt())).thenReturn(true);
        when(logroRepository.calcularXpTotal(usuarioId)).thenReturn(50);

        logroService.procesarActividadParche(usuarioId, UUID.randomUUID(), "TECHNOLOGY");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_CODER, LogroTipo.MONA_CODER.getXp());
        verify(eventPublisher).publicarLogroDesbloqueado(usuarioId, LogroTipo.MONA_CODER, LogroTipo.MONA_CODER.getXp(), 50);
    }

    @Test
    void procesarActividadParche_music_otorgaDjYMusica() {
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), any(LogroTipo.class), anyInt())).thenReturn(true);

        logroService.procesarActividadParche(usuarioId, UUID.randomUUID(), "MUSIC");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_DJ, LogroTipo.MONA_DJ.getXp());
        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_MUSICA, LogroTipo.MONA_MUSICA.getXp());
        verify(logroRepository, never()).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_CULTURA, LogroTipo.MONA_CULTURA.getXp());
    }

    @Test
    void procesarActividadParche_categoriaDesconocida_noOtorgaNada() {
        logroService.procesarActividadParche(usuarioId, UUID.randomUUID(), "NO_EXISTE");

        verify(logroRepository, never()).otorgarSiNoExiste(any(), any(), anyInt());
        verify(senalRepository, never()).registrarParche(any(), any(), any());
    }

    @Test
    void procesarActividadParche_categoriaNull_noOtorgaNiRompeElFlujo() {
        logroService.procesarActividadParche(usuarioId, UUID.randomUUID(), null);

        verify(logroRepository, never()).otorgarSiNoExiste(any(), any(), anyInt());
    }

    @Test
    void procesarActividadParche_study_otorgaEstudiosaAlLlegarATresDistintos() {
        UUID parcheId = UUID.randomUUID();
        when(senalRepository.registrarParche(usuarioId, parcheId, CategoriaActividad.STUDY)).thenReturn(true);
        when(senalRepository.contarParchesDistintosPorCategoria(usuarioId, CategoriaActividad.STUDY)).thenReturn(3L);
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), eq(LogroTipo.MONA_ESTUDIOSA), anyInt())).thenReturn(true);

        logroService.procesarActividadParche(usuarioId, parcheId, "STUDY");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_ESTUDIOSA, LogroTipo.MONA_ESTUDIOSA.getXp());
    }

    @Test
    void procesarActividadParche_study_noOtorgaEstudiosaSiParcheYaRegistrado() {
        UUID parcheId = UUID.randomUUID();
        when(senalRepository.registrarParche(usuarioId, parcheId, CategoriaActividad.STUDY)).thenReturn(false);

        logroService.procesarActividadParche(usuarioId, parcheId, "STUDY");

        verify(senalRepository, never()).contarParchesDistintosPorCategoria(any(), any());
        verify(logroRepository, never()).otorgarSiNoExiste(any(), eq(LogroTipo.MONA_ESTUDIOSA), anyInt());
    }

    @Test
    void procesarActividadParche_variety_otorgaFoodieYRegistraParaTranquila() {
        UUID parcheId = UUID.randomUUID();
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), eq(LogroTipo.MONA_FOODIE), anyInt())).thenReturn(true);

        logroService.procesarActividadParche(usuarioId, parcheId, "VARIETY");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_FOODIE, LogroTipo.MONA_FOODIE.getXp());
        verify(senalRepository).registrarParche(usuarioId, parcheId, CategoriaActividad.VARIETY);
        verify(logroRepository, never()).otorgarSiNoExiste(any(), eq(LogroTipo.MONA_CIENTIFICA), anyInt());
    }

    @Test
    void procesarActividadEvento_variety_otorgaSoloCientifica_noFoodie() {
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), any(LogroTipo.class), anyInt())).thenReturn(true);

        logroService.procesarActividadEvento(usuarioId, UUID.randomUUID(), "VARIETY");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_CIENTIFICA, LogroTipo.MONA_CIENTIFICA.getXp());
        verify(logroRepository, never()).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_FOODIE, LogroTipo.MONA_FOODIE.getXp());
    }

    @Test
    void procesarActividadEvento_entertainment_otorgaSoloCultura_noGamer() {
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), any(LogroTipo.class), anyInt())).thenReturn(true);

        logroService.procesarActividadEvento(usuarioId, UUID.randomUUID(), "ENTERTAINMENT");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_CULTURA, LogroTipo.MONA_CULTURA.getXp());
        verify(logroRepository, never()).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_GAMER, LogroTipo.MONA_GAMER.getXp());
    }

    @Test
    void procesarMatchConfirmado_masDeDiez_otorgaSocial() {
        UUID matchId = UUID.randomUUID();
        when(senalRepository.registrarMatch(usuarioId, matchId)).thenReturn(true);
        when(senalRepository.contarMatches(usuarioId)).thenReturn(11L);
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), eq(LogroTipo.MONA_SOCIAL), anyInt())).thenReturn(true);

        logroService.procesarMatchConfirmado(usuarioId, matchId);

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_SOCIAL, LogroTipo.MONA_SOCIAL.getXp());
    }

    @Test
    void procesarMatchConfirmado_diezOMenos_noOtorgaSocial() {
        UUID matchId = UUID.randomUUID();
        when(senalRepository.registrarMatch(usuarioId, matchId)).thenReturn(true);
        when(senalRepository.contarMatches(usuarioId)).thenReturn(10L);

        logroService.procesarMatchConfirmado(usuarioId, matchId);

        verify(logroRepository, never()).otorgarSiNoExiste(any(), eq(LogroTipo.MONA_SOCIAL), anyInt());
    }

    @Test
    void evaluarBienestar_corteTemprano_noLlamaBienestarSiYaTieneAmbos() {
        when(logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_RESPIRA)).thenReturn(true);
        when(logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_TRANQUILA)).thenReturn(true);

        logroService.procesarActividadEvento(usuarioId, UUID.randomUUID(), "NO_EXISTE");

        verify(bienestarPort, never()).contarEjerciciosCompletados(any());
    }

    @Test
    void evaluarBienestar_bienestarFalla_noOtorgaNiRompeElFlujo() {
        when(logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_RESPIRA)).thenReturn(false);
        when(logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_TRANQUILA)).thenReturn(false);
        when(bienestarPort.contarEjerciciosCompletados(usuarioId)).thenReturn(Optional.empty());

        logroService.procesarActividadEvento(usuarioId, UUID.randomUUID(), "NO_EXISTE");

        verify(logroRepository, never()).otorgarSiNoExiste(any(), eq(LogroTipo.MONA_RESPIRA), anyInt());
        verify(logroRepository, never()).otorgarSiNoExiste(any(), eq(LogroTipo.MONA_TRANQUILA), anyInt());
    }

    @Test
    void evaluarBienestar_umbralAlcanzado_otorgaRespiraYTranquilaSiTieneParcheVariety() {
        when(logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_RESPIRA)).thenReturn(false);
        when(logroRepository.estaDesbloqueado(usuarioId, LogroTipo.MONA_TRANQUILA)).thenReturn(false);
        when(bienestarPort.contarEjerciciosCompletados(usuarioId)).thenReturn(Optional.of(3));
        when(senalRepository.existeParcheCategoria(usuarioId, CategoriaActividad.VARIETY)).thenReturn(true);
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), any(LogroTipo.class), anyInt())).thenReturn(true);

        logroService.procesarActividadEvento(usuarioId, UUID.randomUUID(), "NO_EXISTE");

        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_RESPIRA, LogroTipo.MONA_RESPIRA.getXp());
        verify(logroRepository).otorgarSiNoExiste(usuarioId, LogroTipo.MONA_TRANQUILA, LogroTipo.MONA_TRANQUILA.getXp());
    }

    @Test
    void otorgarSiNuevo_yaExiste_noPublicaEvento() {
        when(logroRepository.otorgarSiNoExiste(eq(usuarioId), eq(LogroTipo.MONA_CODER), anyInt())).thenReturn(false);

        logroService.procesarActividadParche(usuarioId, UUID.randomUUID(), "TECHNOLOGY");

        verify(eventPublisher, never()).publicarLogroDesbloqueado(any(), any(), anyInt(), anyInt());
    }
}
