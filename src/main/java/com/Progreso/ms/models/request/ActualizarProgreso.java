package com.Progreso.ms.models.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

//* DTO para actualizar el progreso de un usuario en un tutorial
//? El id_progreso viene desde el path de la URL, no del body
//! porcentaje_progreso y fecha_ultima_actividad los recalcula el servicio automáticamente
@Data
public class ActualizarProgreso {

    //* Recursos completados hasta el momento de la actualización
    @NotNull
    @Min(0)
    private Integer recursos_completados;

    //* Total de recursos del tutorial (puede cambiar si se agregan recursos)
    @NotNull
    @Positive
    private Integer cantidad_recursos_totales;

    //* Preguntas acertadas acumuladas
    @NotNull
    @Min(0)
    private Integer preguntas_acertadas;

    //* Preguntas falladas acumuladas
    @NotNull
    @Min(0)
    private Integer preguntas_falladas;

}
