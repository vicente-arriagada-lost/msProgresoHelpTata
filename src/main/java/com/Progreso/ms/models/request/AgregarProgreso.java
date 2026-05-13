package com.Progreso.ms.models.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

//* DTO para registrar el progreso inicial de un usuario en un tutorial
//! id_progreso, porcentaje_progreso y fecha_ultima_actividad se omiten:
//! el servicio los calcula y asigna automáticamente
@Data
public class AgregarProgreso {

    //* ID del usuario (referencia al ms-usuario)
    @NotNull
    @Positive
    private Integer id_usuario;

    //* ID del tutorial (referencia al ms-tutoriales)
    @NotNull
    @Positive
    private Integer id_tutorial;

    //* Recursos que el usuario ya completó al momento del registro
    @NotNull
    @Min(0)
    private Integer recursos_completados;

    //* Total de recursos que tiene el tutorial
    @NotNull
    @Positive
    private Integer cantidad_recursos_totales;

    //* Preguntas respondidas correctamente (puede ser 0 al inicio)
    @NotNull
    @Min(0)
    private Integer preguntas_acertadas;

    //* Preguntas respondidas incorrectamente (puede ser 0 al inicio)
    @NotNull
    @Min(0)
    private Integer preguntas_falladas;

}
