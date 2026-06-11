package com.Progreso.ms.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Progreso.ms.models.dto.ProgresoDTO;
import com.Progreso.ms.models.request.ActualizarProgreso;
import com.Progreso.ms.models.request.AgregarProgreso;
import com.Progreso.ms.services.ProgresoService;

import jakarta.validation.Valid;

//* Controlador REST que expone los endpoints de gestión de progreso
//? @RestController = @Controller + @ResponseBody (responde JSON automáticamente)
//? @RequestMapping define el prefijo de todas las rutas de este controlador
@RestController
@RequestMapping("/api/progreso")
public class ProgresoController {

    @Autowired
    private ProgresoService progresoService;

    //* GET /api/progreso — retorna todos los registros de progreso como ProgresoDTO
    @GetMapping
    public ResponseEntity<List<ProgresoDTO>> obtenerTodos() {
        return ResponseEntity.ok(progresoService.obtenerTodosLosProgresos());
    }

    //* GET /api/progreso/{id} — retorna un registro por su ID como ProgresoDTO
    @GetMapping("/{id}")
    public ResponseEntity<ProgresoDTO> obtenerPorId(@PathVariable int id) {
        return ResponseEntity.ok(progresoService.obtenerProgresoPorId(id));
    }

    //* GET /api/progreso/usuario/{idUsuario} — todo el progreso de un usuario como DTOs
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ProgresoDTO>> obtenerPorUsuario(@PathVariable int idUsuario) {
        return ResponseEntity.ok(progresoService.obtenerProgresosPorUsuario(idUsuario));
    }

    //* GET /api/progreso/tutorial/{idTutorial} — todo el progreso sobre un tutorial como DTOs
    @GetMapping("/tutorial/{idTutorial}")
    public ResponseEntity<List<ProgresoDTO>> obtenerPorTutorial(@PathVariable int idTutorial) {
        return ResponseEntity.ok(progresoService.obtenerProgresosPorTutorial(idTutorial));
    }

    //* GET /api/progreso/usuario/{idUsuario}/tutorial/{idTutorial}
    //* Retorna el progreso específico de un usuario en un tutorial como ProgresoDTO
    @GetMapping("/usuario/{idUsuario}/tutorial/{idTutorial}")
    public ResponseEntity<ProgresoDTO> obtenerPorUsuarioYTutorial(@PathVariable int idUsuario,
                                                                   @PathVariable int idTutorial) {
        return ResponseEntity.ok(progresoService.obtenerProgresoPorUsuarioYTutorial(idUsuario, idTutorial));
    }

    //* POST /api/progreso — registra el progreso inicial y retorna el ProgresoDTO creado
    //? @Valid activa las validaciones del DTO de entrada
    //! Responde con HTTP 201 Created en lugar del 200 por defecto
    @PostMapping
    public ResponseEntity<ProgresoDTO> agregar(@Valid @RequestBody AgregarProgreso nuevoProgreso) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(progresoService.agregarProgreso(nuevoProgreso));
    }

    //* PUT /api/progreso/{id} — actualiza el progreso y retorna el ProgresoDTO (con porcentaje recalculado)
    @PutMapping("/{id}")
    public ResponseEntity<ProgresoDTO> actualizar(@PathVariable int id,
                                                  @Valid @RequestBody ActualizarProgreso actProgreso) {
        return ResponseEntity.ok(progresoService.actualizarProgreso(id, actProgreso));
    }

    //* DELETE /api/progreso/{id} — elimina un registro de progreso
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminar(@PathVariable int id) {
        return ResponseEntity.ok(progresoService.eliminarProgreso(id));
    }

}
