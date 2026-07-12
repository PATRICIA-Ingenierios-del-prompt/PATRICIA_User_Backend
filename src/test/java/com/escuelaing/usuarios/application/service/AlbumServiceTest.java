package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.exception.DominioInvalidoException;
import com.escuelaing.usuarios.domain.exception.FotoNoEncontradaException;
import com.escuelaing.usuarios.domain.exception.MaxFotosException;
import com.escuelaing.usuarios.domain.model.Foto;
import com.escuelaing.usuarios.domain.port.outbound.FotoAlbumStoragePort;
import com.escuelaing.usuarios.domain.port.outbound.FotoRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    private FotoAlbumStoragePort fotoStorage;

    @Mock
    private UsuarioEventPublisherPort eventPublisher;

    private AlbumService albumService;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        albumService = new AlbumService(fotoRepository, fotoStorage, eventPublisher);
        usuarioId = UUID.randomUUID();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Foto> fotosExistentes(int cantidad) {
        List<Foto> fotos = new ArrayList<>();
        for (int i = 1; i <= cantidad; i++) {
            fotos.add(Foto.reconstruir(UUID.randomUUID(), usuarioId,
                    "https://fotos/" + i + ".jpg", i, Instant.now()));
        }
        return fotos;
    }

    private static final byte[] BYTES_FAKE = new byte[]{(byte) 0xFF, (byte) 0xD8};
    private static final String MIME_JPEG = "image/jpeg";

    // ── listarFotos ──────────────────────────────────────────────────────────

    @Test
    void listarFotos_retornaFotosDelRepositorio() {
        List<Foto> fotos = fotosExistentes(2);
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(fotos);

        List<Foto> resultado = albumService.listarFotos(usuarioId);

        assertThat(resultado).containsExactlyElementsOf(fotos);
        verify(fotoRepository).buscarPorUsuarioId(usuarioId);
    }

    // ── agregarFoto (bytes) ──────────────────────────────────────────────────

    @Test
    void agregarFoto_laSeptimaFoto_lanzaMaxFotosException() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(fotosExistentes(6));

        assertThatThrownBy(() -> albumService.agregarFoto(usuarioId, BYTES_FAKE, MIME_JPEG))
                .isInstanceOf(MaxFotosException.class);

        verify(fotoStorage, never()).subirFotoAlbum(any(), any(), any());
    }

    @Test
    void agregarFoto_mimeNoSoportado_lanzaDominioInvalidoException() {
        assertThatThrownBy(() -> albumService.agregarFoto(usuarioId, BYTES_FAKE, "image/gif"))
                .isInstanceOf(DominioInvalidoException.class);
    }

    @Test
    void agregarFoto_hastaSeisFotos_seGuardaYPublicaEvento() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(fotosExistentes(5));
        when(fotoStorage.subirFotoAlbum(any(), any(), any())).thenReturn("https://s3/fotos/6.jpg");
        when(fotoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.agregarFoto(usuarioId, BYTES_FAKE, MIME_JPEG);

        assertThat(resultado.getOrden()).isEqualTo(6);
        assertThat(resultado.getUrlFoto()).isEqualTo("https://s3/fotos/6.jpg");
        verify(fotoStorage).subirFotoAlbum(eq(usuarioId), eq(BYTES_FAKE), eq(MIME_JPEG));
        verify(eventPublisher).publicarFotoAgregada(usuarioId, resultado.getId(), 6);
    }

    @Test
    void agregarFoto_primeraFoto_tieneOrden1() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(List.of());
        when(fotoStorage.subirFotoAlbum(any(), any(), any())).thenReturn("https://s3/foto.jpg");
        when(fotoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.agregarFoto(usuarioId, BYTES_FAKE, MIME_JPEG);

        assertThat(resultado.getOrden()).isEqualTo(1);
        assertThat(resultado.esPrincipal()).isTrue();
    }

    // ── agregarFotoDesdeDataUrl ───────────────────────────────────────────────

    @Test
    void agregarFotoDesdeDataUrl_formatoInvalido_lanzaDominioInvalido() {
        assertThatThrownBy(() -> albumService.agregarFotoDesdeDataUrl(usuarioId, "no-es-data-url"))
                .isInstanceOf(DominioInvalidoException.class);
    }

    @Test
    void agregarFotoDesdeDataUrl_base64Invalido_lanzaDominioInvalido() {
        assertThatThrownBy(() ->
                albumService.agregarFotoDesdeDataUrl(usuarioId, "data:image/jpeg;base64,!!!no-base64!!!"))
                .isInstanceOf(DominioInvalidoException.class);
    }

    @Test
    void agregarFotoDesdeDataUrl_valido_subeYPersiste() {
        // "AAAA" es base64 válido (3 bytes)
        String dataUrl = "data:image/png;base64,AAAA";
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(List.of());
        when(fotoStorage.subirFotoAlbum(any(), any(), any())).thenReturn("https://s3/foto.png");
        when(fotoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.agregarFotoDesdeDataUrl(usuarioId, dataUrl);

        assertThat(resultado.getUrlFoto()).isEqualTo("https://s3/foto.png");
        verify(fotoStorage).subirFotoAlbum(eq(usuarioId), any(byte[].class), eq("image/png"));
    }

    // ── actualizarFoto ────────────────────────────────────────────────────────

    @Test
    void actualizarFoto_fotoNoExiste_lanzaFotoNoEncontradaException() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(List.of());

        assertThatThrownBy(() -> albumService.actualizarFoto(usuarioId, UUID.randomUUID(), "https://nueva.jpg"))
                .isInstanceOf(FotoNoEncontradaException.class);
    }

    @Test
    void actualizarFoto_fotoExiste_actualizaUrlCorrectamente() {
        List<Foto> existentes = fotosExistentes(2);
        UUID fotoId = existentes.get(0).getId();
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);
        when(fotoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.actualizarFoto(usuarioId, fotoId, "https://updated.jpg");

        assertThat(resultado.getUrlFoto()).isEqualTo("https://updated.jpg");
        verify(fotoRepository).guardar(any());
    }

    // ── eliminarFoto ──────────────────────────────────────────────────────────

    @Test
    void eliminarFoto_fotoNoExiste_lanzaFotoNoEncontradaException() {
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(List.of());

        assertThatThrownBy(() -> albumService.eliminarFoto(usuarioId, UUID.randomUUID()))
                .isInstanceOf(FotoNoEncontradaException.class);
    }

    @Test
    void eliminarFoto_alEliminarLaPrincipal_promueveAutomaticamenteLaSiguiente() {
        List<Foto> existentes = fotosExistentes(3);
        UUID fotoPrincipalId = existentes.get(0).getId();
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);

        albumService.eliminarFoto(usuarioId, fotoPrincipalId);

        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        verify(fotoRepository).guardarTodas(captor.capture());

        List<Foto> reordenadas = captor.getValue();
        assertThat(reordenadas).hasSize(2);
        assertThat(reordenadas.get(0).esPrincipal()).isTrue();
        assertThat(reordenadas.get(0).getOrden()).isEqualTo(1);

        verify(fotoRepository).eliminar(fotoPrincipalId);
        verify(eventPublisher).publicarFotoEliminada(usuarioId, fotoPrincipalId);
    }

    @Test
    void eliminarFoto_queNoEsLaPrincipal_noAlteraElOrdenDeLaPrincipal() {
        List<Foto> existentes = fotosExistentes(3);
        UUID fotoSecundariaId = existentes.get(2).getId();
        when(fotoRepository.buscarPorUsuarioId(usuarioId)).thenReturn(existentes);

        albumService.eliminarFoto(usuarioId, fotoSecundariaId);

        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        verify(fotoRepository).guardarTodas(captor.capture());

        List<Foto> reordenadas = captor.getValue();
        assertThat(reordenadas).hasSize(2);
        assertThat(reordenadas.get(0).esPrincipal()).isTrue();
        assertThat(reordenadas.get(0).getOrden()).isEqualTo(1);
    }

    // ── marcarPersonaEnFoto ───────────────────────────────────────────────────

    @Test
    void marcarPersonaEnFoto_fotoNoExiste_lanzaFotoNoEncontradaException() {
        UUID fotoId = UUID.randomUUID();
        when(fotoRepository.buscarPorId(fotoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> albumService.marcarPersonaEnFoto(usuarioId, fotoId))
                .isInstanceOf(FotoNoEncontradaException.class);
    }

    @Test
    void marcarPersonaEnFoto_fotoDotroUsuario_lanzaFotoNoEncontradaException() {
        UUID fotoId = UUID.randomUUID();
        UUID otroUsuario = UUID.randomUUID();
        Foto fotoDeOtro = Foto.reconstruir(fotoId, otroUsuario, "https://foto.jpg", 1, Instant.now());
        when(fotoRepository.buscarPorId(fotoId)).thenReturn(Optional.of(fotoDeOtro));

        assertThatThrownBy(() -> albumService.marcarPersonaEnFoto(usuarioId, fotoId))
                .isInstanceOf(FotoNoEncontradaException.class);
    }

    @Test
    void marcarPersonaEnFoto_cambioDefalseATrue_persisteYPublicaEvento() {
        UUID fotoId = UUID.randomUUID();
        Foto foto = Foto.reconstruir(fotoId, usuarioId, "https://foto.jpg", 1, Instant.now(), false);
        when(fotoRepository.buscarPorId(fotoId)).thenReturn(Optional.of(foto));
        when(fotoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        Foto resultado = albumService.marcarPersonaEnFoto(usuarioId, fotoId);

        assertThat(resultado.isTienePersonaEnFoto()).isTrue();
        verify(fotoRepository).guardar(foto);
        verify(eventPublisher).publicarPersonaDetectadaEnFoto(usuarioId, fotoId);
    }

    @Test
    void marcarPersonaEnFoto_yaEstaMarcada_noVuelveAPublicarEvento() {
        UUID fotoId = UUID.randomUUID();
        Foto foto = Foto.reconstruir(fotoId, usuarioId, "https://foto.jpg", 1, Instant.now(), true);
        when(fotoRepository.buscarPorId(fotoId)).thenReturn(Optional.of(foto));
        when(fotoRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));

        albumService.marcarPersonaEnFoto(usuarioId, fotoId);

        // Ya estaba en true → no se vuelve a publicar
        verify(eventPublisher, never()).publicarPersonaDetectadaEnFoto(any(), any());
    }
}
