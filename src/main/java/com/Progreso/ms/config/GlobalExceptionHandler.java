package com.Progreso.ms.config;

import com.Progreso.ms.clients.LogClient;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private LogClient logClient;

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        int status = ex.getStatusCode().value();
        String tipo = status >= 500 ? "ERROR" : "WARNING";
        logger.warn("Error HTTP {} en {}: {}", status, request.getRequestURI(), ex.getReason());
        logClient.registrar(tipo,
                "Error HTTP " + status + " en " + request.getRequestURI(),
                null, ex.getReason());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex, HttpServletRequest request) {
        logger.error("Error inesperado en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        logClient.registrar("ERROR",
                "Error inesperado en " + request.getRequestURI(),
                null, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 500);
        body.put("error", "Error interno del servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
