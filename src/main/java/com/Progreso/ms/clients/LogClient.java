package com.Progreso.ms.clients;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

//* Cliente REST que envía eventos al MS Logs (puerto 8081)
//* Fire-and-forget: todas las excepciones se capturan internamente
@Component
public class LogClient {

    private static final Logger logger = LoggerFactory.getLogger(LogClient.class);
    private static final String SERVICIO = "ms-progreso";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ms.logs.url:http://localhost:8081}")
    private String logsUrl;

    public void registrar(String tipo, String mensaje, Integer idUsuario, String detalle) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("tipo_log", tipo);
            body.put("servicio_origen", SERVICIO);
            body.put("mensaje_log", mensaje);
            if (idUsuario != null) body.put("id_usuario", idUsuario);
            if (detalle != null) body.put("detalle_log", detalle);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.postForObject(
                logsUrl + "/api/logs",
                new HttpEntity<>(body, headers),
                Object.class
            );
        } catch (Exception e) {
            logger.warn("No se pudo registrar log en ms-Logs: {}", e.getMessage());
        }
    }
}
