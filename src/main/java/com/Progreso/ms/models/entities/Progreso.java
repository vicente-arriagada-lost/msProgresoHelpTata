package com.Progreso.ms.models.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

//* Entidad JPA que representa la tabla "progreso" en la base de datos
//? @Data de Lombok genera automáticamente getters, setters, equals, hashCode y toString
@Entity
@Data
@Table(name = "progreso")
public class Progreso {

    //* Clave primaria autoincremental
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_progreso;

    //* ID del usuario dueño de este progreso (referencia al ms-usuario)
    @Column(nullable = false)
    private int id_usuario;

    //* ID del tutorial al que pertenece este progreso (referencia al ms-tutoriales)
    @Column(nullable = false)
    private int id_tutorial;

    //* Cantidad de recursos que el usuario ya completó
    @Column(nullable = false)
    private int recursos_completados;

    //* Total de recursos que tiene el tutorial
    @Column(nullable = false)
    private int cantidad_recursos_totales;

    //* Cantidad de preguntas o ejercicios respondidos correctamente
    @Column(nullable = false)
    private int preguntas_acertadas;

    //* Cantidad de preguntas o ejercicios respondidos incorrectamente
    @Column(nullable = false)
    private int preguntas_falladas;

    //* Porcentaje de avance calculado automáticamente en el servicio
    //? Formula: (recursos_completados / cantidad_recursos_totales) * 100
    @Column(nullable = false)
    private double porcentaje_progreso;

    //* Fecha y hora de la ultima actividad registrada — actualizada en cada modificacion
    @Column(nullable = false)
    private LocalDateTime fecha_ultima_actividad;

}
