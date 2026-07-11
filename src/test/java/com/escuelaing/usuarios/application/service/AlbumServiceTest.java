package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.FotoNoEncontradaException;
import com.escuelaing.usuarios.domain.exception.MaxFotosException;
import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.outbound.FotoRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private FotoRepositoryPort fotoRepository;

    @Mock
    private UsuarioEventPublisherPort eventPublisher;

    private AlbumService albumService;

    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        albumService = new AlbumService(fotoRepository, eventPublisher);
        usuarioId = UUID.randomUUID();
    }

    private List<Foto> fotosExistentes(int cantidad) {
        List<Foto> fotos = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            fotos.add(Foto.reconstruir(UUID.randomUUID(), usuarioId, "https://fotos/" + i + ".jpg",
                    i, java.time.Instant.now()));
        }
        return fotos;
    }

    @Test
    void listarFotos_retornaFotosDelRepositorio() {
        List<Foto> fotos = fotosExistentes(2);
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(fotos);

        List<Foto> resultado = albumService.listarFotos(usuarioId);

        assertThat(resultado).containsExactlyElementsOf(fotos);
        verify(fotoRepository, times(1)).buscarPorUsuarioId(usuarioId);
    }

    @Test
    void agregarFoto_laSeptimaFoto_lanzaMaxFotosException() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(fotosExistentes(6));

        assertThatThrownBy(() -> albumService.agregarFoto(usuarioId, "https://fotos/7.jpg"))
                .isInstanceOf(MaxFotosException.class);
    }

    @Test
    void agregarFoto_hastaSeisFotos_seGuardaCorrectamente() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(fotosExistentes(5));
        when(fotoRepository.guardar(any(Foto.class))).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.agregarFoto(usuarioId, "https://fotos/6.jpg");

        assertThat(resultado.getOrden()).isEqualTo(6);
        verify(eventPublisher).publicarFotoAgregada(usuarioId, resultado.getId(), 6);
    }

    @Test
    void actualizarFoto_fotoNoExiste_lanzaFotoNoEncontradaException() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(List.of());

        assertThatThrownBy(() -> albumService.actualizarFoto(usuarioId, UUID.randomUUID(), "https://fotos/updated.jpg"))
                .isInstanceOf(FotoNoEncontradaException.class);
    }

    @Test
    void actualizarFoto_fotoExiste_actualizaUrlCorrectamente() {
        List<Foto> existentes = fotosExistentes(2);
        UUID fotoId = existentes.get(0).getId();
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);
        when(fotoRepository.guardar(any(Foto.class))).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.actualizarFoto(usuarioId, fotoId, "https://fotos/updated.jpg");

        assertThat(resultado.getUrlFoto()).isEqualTo("https://fotos/updated.jpg");
        verify(fotoRepository, times(1)).guardar(any(Foto.class));
    }

    @Test
    void eliminarFoto_fotoNoExiste_lanzaFotoNoEncontradaException() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(List.of());

        assertThatThrownBy(() -> albumService.eliminarFoto(usuarioId, UUID.randomUUID()))
                .isInstanceOf(FotoNoEncontradaException.class);
    }

    @Test
    void eliminarFoto_alEliminarLaPrincipal_promueveAutomaticamenteLaSiguiente() {
        List<Foto> existentes = fotosExistentes(3); // orden 1, 2, 3
        UUID fotoPrincipalId = existentes.get(0).getId();

        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);

        albumService.eliminarFoto(usuarioId, fotoPrincipalId);

        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        verify(fotoRepository).guardarTodas(captor.capture());

        List<Foto> reordenadas = captor.getValue();
        assertThat(reordenadas).hasSize(2);
        assertThat(reordenadas.get(0).getOrden()).isEqualTo(1);
        assertThat(reordenadas.get(0).esPrincipal()).isTrue();

        verify(fotoRepository).eliminar(fotoPrincipalId);
        verify(eventPublisher).publicarFotoEliminada(usuarioId, fotoPrincipalId);
    }

    @Test
    void eliminarFoto_queNoEsLaPrincipal_noAlteraElOrdenDeLaPrincipal() {
        List<Foto> existentes = fotosExistentes(3); // orden 1, 2, 3
        UUID fotoSecundariaId = existentes.get(2).getId(); // orden 3

        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);

        albumService.eliminarFoto(usuarioId, fotoSecundariaId);

        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        verify(fotoRepository).guardarTodas(captor.capture());

        List<Foto> reordenadas = captor.getValue();
        assertThat(reordenadas).hasSize(2);
        assertThat(reordenadas.get(0).esPrincipal()).isTrue();
        assertThat(reordenadas.get(0).getOrden()).isEqualTo(1);
    }
}
