package com.escuelaing.usuarios.infrastructure.client;

import com.escuelaing.usuarios.domain.port.outbound.PersonaDetectorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Adaptador que implementa {@link PersonaDetectorPort} llamando al sidecar
 * Python (DeepFace/RetinaFace) que corre en localhost:8090 dentro del mismo
 * contenedor Docker.
 *
 * Si el sidecar no está disponible (aún arrancando, error transitorio) se
 * devuelve {@code false} y se registra el error — el flujo de subida de foto
 * no se interrumpe, y la detección puede re-intentarse después mediante el
 * endpoint PUT /{id}/fotos/{fotoId}/persona.
 */
@Component
public class PersonaDetectorAdapter implements PersonaDetectorPort {

    private static final Logger log = LoggerFactory.getLogger(PersonaDetectorAdapter.class);

    private final RestTemplate restTemplate;
    private final String detectorUrl;

    public PersonaDetectorAdapter(
            @Value("${person-detector.url:http://localhost:8090}") String detectorUrl) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);   // 5 seconds
        factory.setReadTimeout(30_000);     // 30 seconds

        this.restTemplate = new RestTemplate(factory);
        this.detectorUrl = detectorUrl;
    }

    @Override
    public boolean tienPersona(String urlFoto) {
        try {
            var body = Map.of("url_foto", urlFoto);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                    detectorUrl + "/detect",
                    body,
                    Map.class
            );
            if (response == null) {
                log.warn("El detector de personas devolvió respuesta nula para: {}", urlFoto);
                return false;
            }
            Object result = response.get("tiene_persona");
            return Boolean.TRUE.equals(result);
        } catch (Exception ex) {
            log.error("Error al llamar al detector de personas (url={}): {}", urlFoto, ex.getMessage());
            return false;
        }
    }
}
