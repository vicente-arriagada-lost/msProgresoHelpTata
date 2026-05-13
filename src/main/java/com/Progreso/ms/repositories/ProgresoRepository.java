package com.Progreso.ms.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Progreso.ms.models.entities.Progreso;

import java.util.List;
import java.util.Optional;

//* Repositorio JPA para la entidad Progreso
//? JpaRepository<Progreso, Integer> provee automáticamente:
//? findAll(), findById(), save(), deleteById(), existsById(), count(), etc.
//? Se usan @Query explícitas porque los campos con guión bajo generan ambigüedad
//? en los métodos derivados (Spring Data interpreta '_' como separador de relaciones)
@Repository
public interface ProgresoRepository extends JpaRepository<Progreso, Integer> {

    //* Retorna todos los registros de progreso de un usuario específico
    @Query("SELECT p FROM Progreso p WHERE p.id_usuario = :idUsuario")
    List<Progreso> findByIdUsuario(@Param("idUsuario") int idUsuario);

    //* Retorna todos los registros de progreso de un tutorial específico
    @Query("SELECT p FROM Progreso p WHERE p.id_tutorial = :idTutorial")
    List<Progreso> findByIdTutorial(@Param("idTutorial") int idTutorial);

    //* Retorna el progreso de un usuario en un tutorial concreto
    //? Optional porque puede no existir (el usuario no ha iniciado ese tutorial)
    @Query("SELECT p FROM Progreso p WHERE p.id_usuario = :idUsuario AND p.id_tutorial = :idTutorial")
    Optional<Progreso> findByIdUsuarioAndIdTutorial(@Param("idUsuario") int idUsuario,
                                                    @Param("idTutorial") int idTutorial);

}
