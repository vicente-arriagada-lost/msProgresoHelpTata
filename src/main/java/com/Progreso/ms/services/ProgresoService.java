package com.Progreso.ms.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Progreso.ms.models.entities.Progreso;
import com.Progreso.ms.models.request.ActualizarProgreso;
import com.Progreso.ms.models.request.AgregarProgreso;
import com.Progreso.ms.repositories.ProgresoRepository;

//* Servicio que encapsula toda la lógica de negocio relacionada al progreso de usuarios
//? @Service marca esta clase para que Spring la detecte e inyecte donde se necesite
@Service
public class ProgresoService {

    @Autowired
    private ProgresoRepository progresoRepository;

    //* Retorna todos los registros de progreso del sistema
    public List<Progreso> obtenerTodosLosProgresos() {
        return progresoRepository.findAll();
    }

    //* Busca un registro de progreso por su ID
    //! Lanza HTTP 404 si el ID no existe
    public Progreso obtenerProgresoPorId(int id_progreso) {
        return progresoRepository.findById(id_progreso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado."));
    }

    //* Retorna todos los registros de progreso de un usuario específico
    public List<Progreso> obtenerProgresosPorUsuario(int idUsuario) {
        return progresoRepository.findByIdUsuario(idUsuario);
    }

    //* Retorna todos los registros de progreso de un tutorial específico
    public List<Progreso> obtenerProgresosPorTutorial(int idTutorial) {
        return progresoRepository.findByIdTutorial(idTutorial);
    }

    //* Retorna el progreso de un usuario en un tutorial concreto
    //! Lanza HTTP 404 si el usuario no ha iniciado ese tutorial
    public Progreso obtenerProgresoPorUsuarioYTutorial(int idUsuario, int idTutorial) {
        return progresoRepository.findByIdUsuarioAndIdTutorial(idUsuario, idTutorial)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró progreso para el usuario " + idUsuario
                        + " en el tutorial " + idTutorial + "."));
    }

    //* Registra el progreso inicial de un usuario en un tutorial
    //! Lanza HTTP 409 si ya existe un registro para ese usuario+tutorial
    //! porcentaje_progreso y fecha_ultima_actividad los asigna este método
    public Progreso agregarProgreso(AgregarProgreso nuevoProgreso) {
        Optional<Progreso> existente = progresoRepository
                .findByIdUsuarioAndIdTutorial(nuevoProgreso.getId_usuario(), nuevoProgreso.getId_tutorial());
        if (existente.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un registro de progreso para ese usuario y tutorial.");
        }
        Progreso progreso = new Progreso();
        progreso.setId_usuario(nuevoProgreso.getId_usuario());
        progreso.setId_tutorial(nuevoProgreso.getId_tutorial());
        progreso.setRecursos_completados(nuevoProgreso.getRecursos_completados());
        progreso.setCantidad_recursos_totales(nuevoProgreso.getCantidad_recursos_totales());
        progreso.setPreguntas_acertadas(nuevoProgreso.getPreguntas_acertadas());
        progreso.setPreguntas_falladas(nuevoProgreso.getPreguntas_falladas());
        //* Calcula el porcentaje de avance sobre los recursos completados
        progreso.setPorcentaje_progreso(calcularPorcentaje(
                nuevoProgreso.getRecursos_completados(),
                nuevoProgreso.getCantidad_recursos_totales()));
        progreso.setFecha_ultima_actividad(LocalDateTime.now());
        return progresoRepository.save(progreso);
    }

    //* Actualiza el progreso de un usuario — recalcula porcentaje y timestamp
    public Progreso actualizarProgreso(int id_progreso, ActualizarProgreso actProgreso) {
        Progreso progreso = progresoRepository.findById(id_progreso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado."));
        progreso.setRecursos_completados(actProgreso.getRecursos_completados());
        progreso.setCantidad_recursos_totales(actProgreso.getCantidad_recursos_totales());
        progreso.setPreguntas_acertadas(actProgreso.getPreguntas_acertadas());
        progreso.setPreguntas_falladas(actProgreso.getPreguntas_falladas());
        //* Recalcula el porcentaje y actualiza el timestamp en cada modificación
        progreso.setPorcentaje_progreso(calcularPorcentaje(
                actProgreso.getRecursos_completados(),
                actProgreso.getCantidad_recursos_totales()));
        progreso.setFecha_ultima_actividad(LocalDateTime.now());
        return progresoRepository.save(progreso);
    }

    //* Elimina un registro de progreso por su ID
    //! Lanza HTTP 404 si el ID no existe
    public String eliminarProgreso(int id_progreso) {
        if (progresoRepository.existsById(id_progreso)) {
            progresoRepository.deleteById(id_progreso);
            return "Progreso eliminado correctamente.";
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado.");
        }
    }

    //* Calcula el porcentaje de progreso sobre recursos completados
    //? Evita división por cero: retorna 0.0 si el total es 0
    private double calcularPorcentaje(int completados, int totales) {
        if (totales == 0) return 0.0;
        return Math.round((completados * 100.0 / totales) * 100.0) / 100.0;
    }

}
