package com.escuelaing.usuarios.infrastructure.client;

import com.escuelaing.usuarios.domain.port.outbound.PersonaDetectorPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Adaptador que implementa {@link PersonaDetectorPort} llamando al sidecar
 * Python (DeepFace/RetinaFace) que corre en localhost:8090 dentro del mismo
 * contenedor Docker.
 *
 * La llamada es síncrona a propósito: la respuesta de subida de foto debe
 * reflejar si cumple o no en el mismo request, sin que el frontend necesite
 * recargar para verlo. Para que eso sea confiable, se reintenta un par de
 * veces ante fallas de conexión (p.ej. justo después de un arranque de
 * contenedor, cuando supervisord ya levantó el proceso Java pero el sidecar
 * de Python/TensorFlow todavía está importando/cargando el modelo). Un error
 * HTTP con respuesta real del sidecar (imagen no descargable, etc.) NO se
 * reintenta, porque no es un problema de disponibilidad.
 */
@Component
public class PersonaDetectorAdapter implements PersonaDetectorPort {

    private static final Logger log = LoggerFactory.getLogger(PersonaDetectorAdapter.class);
    private static final int MAX_INTENTOS = 3;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 700;

    private final RestTemplate restTemplate;
    private final String detectorUrl;

    public PersonaDetectorAdapter(
            @Value("${person-detector.url:http://localhost:8090}") String detectorUrl) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(8_000);

        this.restTemplate = new RestTemplate(factory);
        this.detectorUrl = detectorUrl;
    }

    @Override
    public boolean tienPersona(String urlFoto) {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
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
            } catch (ResourceAccessException ex) {
                // Falla de conexión/timeout: el sidecar puede estar todavía
                // arrancando. Vale la pena reintentar un par de veces.
                log.warn("Intento {}/{} fallido llamando al detector de personas (url={}): {}",
                        intento, MAX_INTENTOS, urlFoto, ex.getMessage());
                if (intento == MAX_INTENTOS) {
                    log.error("El detector de personas no respondió tras {} intentos (url={})",
                            MAX_INTENTOS, urlFoto);
                    return false;
                }
                dormir(ESPERA_ENTRE_INTENTOS_MS);
            } catch (Exception ex) {
                // Respuesta de error real del sidecar (p.ej. imagen no
                // descargable): no es un problema de disponibilidad, no
                // tiene sentido reintentar.
                log.error("Error al llamar al detector de personas (url={}): {}", urlFoto, ex.getMessage());
                return false;
            }
        }
        return false;
    }

    private void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
