package com.Progreso.ms.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.Progreso.ms.clients.LogClient;
import com.Progreso.ms.models.dto.ProgresoDTO;
import com.Progreso.ms.models.entities.Progreso;
import com.Progreso.ms.models.request.ActualizarProgreso;
import com.Progreso.ms.models.request.AgregarProgreso;
import com.Progreso.ms.repositories.ProgresoRepository;

//* Servicio que encapsula toda la lógica de negocio relacionada al progreso de usuarios
@Service
public class ProgresoService {

    @Autowired
    private ProgresoRepository progresoRepository;

    @Autowired
    private LogClient logClient;

    //* Convierte una entidad Progreso a su DTO de respuesta
    private ProgresoDTO toDTO(Progreso p) {
        return new ProgresoDTO(
                p.getId_progreso(),
                p.getId_usuario(),
                p.getId_tutorial(),
                p.getRecursos_completados(),
                p.getCantidad_recursos_totales(),
                p.getPreguntas_acertadas(),
                p.getPreguntas_falladas(),
                p.getPorcentaje_progreso(),
                p.getFecha_ultima_actividad()
        );
    }

    //* Retorna todos los registros de progreso como DTOs
    public List<ProgresoDTO> obtenerTodosLosProgresos() {
        return progresoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    //* Busca un registro de progreso por su ID y retorna el DTO
    //! Lanza HTTP 404 si el ID no existe
    public ProgresoDTO obtenerProgresoPorId(int id_progreso) {
        Progreso progreso = progresoRepository.findById(id_progreso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado."));
        return toDTO(progreso);
    }

    //* Retorna todos los registros de progreso de un usuario específico como DTOs
    public List<ProgresoDTO> obtenerProgresosPorUsuario(int idUsuario) {
        return progresoRepository.findByIdUsuario(idUsuario)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    //* Retorna todos los registros de progreso de un tutorial específico como DTOs
    public List<ProgresoDTO> obtenerProgresosPorTutorial(int idTutorial) {
        return progresoRepository.findByIdTutorial(idTutorial)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    //* Retorna el progreso de un usuario en un tutorial concreto como DTO
    //! Lanza HTTP 404 si el usuario no ha iniciado ese tutorial
    public ProgresoDTO obtenerProgresoPorUsuarioYTutorial(int idUsuario, int idTutorial) {
        Progreso progreso = progresoRepository.findByIdUsuarioAndIdTutorial(idUsuario, idTutorial)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No se encontró progreso para el usuario " + idUsuario
                        + " en el tutorial " + idTutorial + "."));
        return toDTO(progreso);
    }

    //* Registra el progreso inicial de un usuario en un tutorial y retorna el DTO
    //! Lanza HTTP 409 si ya existe un registro para ese usuario+tutorial
    public ProgresoDTO agregarProgreso(AgregarProgreso nuevoProgreso) {
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
        progreso.setPorcentaje_progreso(calcularPorcentaje(
                nuevoProgreso.getRecursos_completados(),
                nuevoProgreso.getCantidad_recursos_totales()));
        progreso.setFecha_ultima_actividad(LocalDateTime.now());
        ProgresoDTO resultado = toDTO(progresoRepository.save(progreso));
        logClient.registrar("INFO",
                "Progreso iniciado: usuario=" + nuevoProgreso.getId_usuario()
                        + ", tutorial=" + nuevoProgreso.getId_tutorial()
                        + " (" + resultado.porcentaje_progreso() + "%)",
                nuevoProgreso.getId_usuario(), null);
        return resultado;
    }

    //* Actualiza el progreso de un usuario — recalcula porcentaje y timestamp
    public ProgresoDTO actualizarProgreso(int id_progreso, ActualizarProgreso actProgreso) {
        Progreso progreso = progresoRepository.findById(id_progreso)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Progreso no encontrado."));
        progreso.setRecursos_completados(actProgreso.getRecursos_completados());
        progreso.setCantidad_recursos_totales(actProgreso.getCantidad_recursos_totales());
        progreso.setPreguntas_acertadas(actProgreso.getPreguntas_acertadas());
        progreso.setPreguntas_falladas(actProgreso.getPreguntas_falladas());
        progreso.setPorcentaje_progreso(calcularPorcentaje(
                actProgreso.getRecursos_completados(),
                actProgreso.getCantidad_recursos_totales()));
        progreso.setFecha_ultima_actividad(LocalDateTime.now());
        ProgresoDTO resultado = toDTO(progresoRepository.save(progreso));
        logClient.registrar("INFO",
                "Progreso actualizado: usuario=" + progreso.getId_usuario()
                        + ", tutorial=" + progreso.getId_tutorial()
                        + " → " + resultado.porcentaje_progreso() + "%",
                progreso.getId_usuario(), null);
        return resultado;
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
    private double calcularPorcentaje(int completados, int totales) {
        if (totales == 0) return 0.0;
        return Math.round((completados * 100.0 / totales) * 100.0) / 100.0;
    }
}
