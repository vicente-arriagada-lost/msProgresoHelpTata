package com.Progreso.ms.models.dto;

import java.time.LocalDateTime;

// DTO de respuesta para Progreso
public record ProgresoDTO(
        int id_progreso,
        int id_usuario,
        int id_tutorial,
        int recursos_completados,
        int cantidad_recursos_totales,
        int preguntas_acertadas,
        int preguntas_falladas,
        double porcentaje_progreso,
        LocalDateTime fecha_ultima_actividad
) {}
