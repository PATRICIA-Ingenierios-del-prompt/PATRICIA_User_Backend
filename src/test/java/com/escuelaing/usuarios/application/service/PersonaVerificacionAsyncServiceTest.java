package com.escuelaing.usuarios.application.service;

import com.escuelaing.usuarios.domain.model.Perfil;
import com.escuelaing.usuarios.domain.port.outbound.PerfilRepositoryPort;
import com.escuelaing.usuarios.domain.port.outbound.PersonaDetectorPort;
import com.escuelaing.usuarios.domain.port.outbound.UsuarioEventPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaVerificacionAsyncServiceTest {

    @Mock private PersonaDetectorPort personaDetector;
    @Mock private PerfilRepositoryPort perfilRepository;
    @Mock private UsuarioEventPublisherPort eventPublisher;

    private PersonaVerificacionAsyncService service;
    private UUID usuarioId;
    private static final String URL = "https://s3/foto.jpg";

    @BeforeEach
    void setUp() {
        service = new PersonaVerificacionAsyncService(personaDetector, perfilRepository, eventPublisher);
        usuarioId = UUID.randomUUID();
    }

    @Test
    void noDetectaPersona_noGuardaNiPublica() {
        when(personaDetector.tienPersona(URL)).thenReturn(false);

        service.verificarPersonaEnFoto(usuarioId, URL);

        verify(perfilRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarPersonaDetectadaEnFoto(any(), any());
    }

    @Test
    void detectaPersona_perfilYaNoExiste_noHaceNada() {
        when(personaDetector.tienPersona(URL)).thenReturn(true);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.empty());

        service.verificarPersonaEnFoto(usuarioId, URL);

        verify(perfilRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarPersonaDetectadaEnFoto(any(), any());
    }

    @Test
    void detectaPersona_fotoCambioDeNuevo_descartaResultadoObsoleto() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        perfil.actualizarUrlFotoPerfil("https://s3/otra-foto-mas-reciente.jpg");

        when(personaDetector.tienPersona(URL)).thenReturn(true);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        service.verificarPersonaEnFoto(usuarioId, URL);

        verify(perfilRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarPersonaDetectadaEnFoto(any(), any());
    }

    @Test
    void detectaPersona_fotoSigueVigente_marcaGuardaYPublica() {
        Perfil perfil = Perfil.crearVacio(usuarioId);
        perfil.actualizarUrlFotoPerfil(URL);

        when(personaDetector.tienPersona(URL)).thenReturn(true);
        when(perfilRepository.buscarPorUsuarioId(usuarioId)).thenReturn(Optional.of(perfil));

        service.verificarPersonaEnFoto(usuarioId, URL);

        verify(perfilRepository).guardar(perfil);
        verify(eventPublisher).publicarPersonaDetectadaEnFoto(eq(usuarioId), eq(perfil.getId()));
    }

    @Test
    void detectorLanzaExcepcion_noPropaga() {
        when(personaDetector.tienPersona(URL)).thenThrow(new RuntimeException("timeout"));

        service.verificarPersonaEnFoto(usuarioId, URL);

        verify(perfilRepository, never()).guardar(any());
    }
}
