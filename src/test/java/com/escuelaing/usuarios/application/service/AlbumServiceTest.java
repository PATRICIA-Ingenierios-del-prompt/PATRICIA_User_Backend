package com.escuelaing.usuarios.application.service;

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
import static org.mockito.Mockito.when;

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
        org.mockito.Mockito.verify(eventPublisher)
                .publicarFotoAgregada(usuarioId, resultado.getId(), 6);
    }

    @Test
    void eliminarFoto_alEliminarLaPrincipal_promueveAutomaticamenteLaSiguiente() {
        List<Foto> existentes = fotosExistentes(3); // orden 1, 2, 3
        UUID fotoPrincipalId = existentes.get(0).getId();

        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);

        albumService.eliminarFoto(usuarioId, fotoPrincipalId);

        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(fotoRepository).guardarTodas(captor.capture());

        List<Foto> reordenadas = captor.getValue();
        assertThat(reordenadas).hasSize(2);
        // La foto que antes tenía orden 2 ahora debe ser la principal (orden 1).
        assertThat(reordenadas.get(0).getOrden()).isEqualTo(1);
        assertThat(reordenadas.get(0).esPrincipal()).isTrue();

        org.mockito.Mockito.verify(fotoRepository).eliminar(fotoPrincipalId);
        org.mockito.Mockito.verify(eventPublisher).publicarFotoEliminada(usuarioId, fotoPrincipalId);
    }

    @Test
    void eliminarFoto_queNoEsLaPrincipal_noAlteraElOrdenDeLaPrincipal() {
        List<Foto> existentes = fotosExistentes(3); // orden 1, 2, 3
        UUID fotoSecundariaId = existentes.get(2).getId(); // orden 3

        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);

        albumService.eliminarFoto(usuarioId, fotoSecundariaId);

        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(fotoRepository).guardarTodas(captor.capture());

        List<Foto> reordenadas = captor.getValue();
        assertThat(reordenadas).hasSize(2);
        assertThat(reordenadas.get(0).esPrincipal()).isTrue();
        assertThat(reordenadas.get(0).getOrden()).isEqualTo(1);
    }
}
