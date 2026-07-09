package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.InteresInvalidoException;
import com.escuelaing.usuarios.domain.model.Disponibilidad;
import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.outbound.FotoPerfilStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepositoryPort perfilRepository;

    @Mock
    private UsuarioEventPublisherPort eventPublisher;

    @Mock
    private FotoPerfilStoragePort fotoPerfilStorage;

    private PerfilService perfilService;

    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        perfilService = new PerfilService(perfilRepository, eventPublisher, fotoPerfilStorage);
        usuarioId = UUID.randomUUID();
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

        // Mismos valores que ya tiene el perfil recién creado (disponibilidad DISPONIBLE, resto null/vacío).
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
    void actualizarPerfil_publicaDisponibilidadCambiada_soloCuandoCambiaEseCampo() {
        Perfil perfil = Perfil.crearVacio(usuarioId); // disponibilidad inicial: DISPONIBLE
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        // Cambia disponibilidad junto con otro campo.
        perfilService.actualizarPerfil(usuarioId, "Bio nueva", null, null, List.of(), Disponibilidad.OCUPADO);

        verify(eventPublisher, times(1))
                .publicarDisponibilidadCambiada(usuarioId, Disponibilidad.OCUPADO.name());
    }

    @Test
    void actualizarPerfil_noPublicaDisponibilidadCambiada_cuandoSoloCambianOtrosCampos() {
        Perfil perfil = Perfil.crearVacio(usuarioId); // disponibilidad inicial: DISPONIBLE
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));
        when(perfilRepository.guardar(any(Perfil.class))).thenAnswer(inv -> inv.getArgument(0));

        // Solo cambia la bio; la disponibilidad se mantiene igual (DISPONIBLE).
        perfilService.actualizarPerfil(usuarioId, "Bio nueva", null, null, List.of(), Disponibilidad.DISPONIBLE);

        verify(eventPublisher, never()).publicarDisponibilidadCambiada(any(), any());
        verify(eventPublisher, times(1)).publicarPerfilActualizado(eq(usuarioId), eq(List.of("bio")));
    }
}
