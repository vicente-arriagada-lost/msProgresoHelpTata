package com.Progreso.ms;

// =============================================================
// TESTS UNITARIOS — ProgresoServiceTest.java
// =============================================================
// Pruebas de la lógica de negocio de ProgresoService usando Mockito.
//
// Casos especiales probados:
//   agregarProgreso_calculaPorcentaje — 3 recursos completados de 5
//   totales debe resultar en 60.0% (no 60 ni 0.6).
//
//   agregarProgreso_totalCero_porcentajeCero — si total=0, el
//   divisor es 0 y podría lanzar ArithmeticException. El servicio
//   debe manejar esto retornando 0.0.
//
//   agregarProgreso_duplicado_lanza409 — un usuario no puede tener
//   dos registros de progreso para el mismo tutorial. El servicio
//   debe rechazarlo con HTTP 409 Conflict.
//
// Ejecutar con: ./mvnw test
// =============================================================

import com.Progreso.ms.models.dto.ProgresoDTO;
import com.Progreso.ms.models.entities.Progreso;
import com.Progreso.ms.models.request.AgregarProgreso;
import com.Progreso.ms.models.request.ActualizarProgreso;
import com.Progreso.ms.repositories.ProgresoRepository;
import com.Progreso.ms.services.ProgresoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

//* @ExtendWith habilita la integración de Mockito con JUnit 5
@ExtendWith(MockitoExtension.class)
@DisplayName("ProgresoService — pruebas unitarias")
class ProgresoServiceTest {

    @Mock
    private ProgresoRepository progresoRepository;

    //* @InjectMocks crea una instancia real del servicio e inyecta el mock
    @InjectMocks
    private ProgresoService progresoService;

    //* Progreso base reutilizado en los tests para evitar repetición
    private Progreso progresoBase;

    @BeforeEach
    void setUp() {
        //* Prepara un registro de progreso con 3/5 recursos = 60%
        progresoBase = new Progreso();
        progresoBase.setId_progreso(1);
        progresoBase.setId_usuario(1);
        progresoBase.setId_tutorial(1);
        progresoBase.setRecursos_completados(3);
        progresoBase.setCantidad_recursos_totales(5);
        progresoBase.setPreguntas_acertadas(8);
        progresoBase.setPreguntas_falladas(2);
        progresoBase.setPorcentaje_progreso(60.0);
        progresoBase.setFecha_ultima_actividad(LocalDateTime.now());
    }

    // ── obtenerTodosLosProgresos ───────────────────────────────────────────────

    @Test
    @DisplayName("obtenerTodosLosProgresos: retorna lista con todos los registros")
    void obtenerTodosLosProgresos_retornaLista() {
        //* Arrange
        when(progresoRepository.findAll()).thenReturn(List.of(progresoBase));

        //* Act
        List<ProgresoDTO> resultado = progresoService.obtenerTodosLosProgresos();

        //* Assert
        assertEquals(1, resultado.size());
        assertEquals(1, resultado.get(0).id_usuario());
        verify(progresoRepository, times(1)).findAll();
    }

    // ── obtenerProgresoPorId ───────────────────────────────────────────────────

    @Test
    @DisplayName("obtenerProgresoPorId: retorna DTO cuando el ID existe")
    void obtenerProgresoPorId_existente() {
        //* Arrange
        when(progresoRepository.findById(1)).thenReturn(Optional.of(progresoBase));

        //* Act
        ProgresoDTO resultado = progresoService.obtenerProgresoPorId(1);

        //* Assert: porcentaje debe ser el pre-calculado en setUp
        assertNotNull(resultado);
        assertEquals(60.0, resultado.porcentaje_progreso());
    }

    @Test
    @DisplayName("obtenerProgresoPorId: lanza 404 si el ID no existe")
    void obtenerProgresoPorId_noExiste_lanza404() {
        //* Arrange
        when(progresoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> progresoService.obtenerProgresoPorId(99));
    }

    // ── agregarProgreso ────────────────────────────────────────────────────────

    @Test
    @DisplayName("agregarProgreso: calcula porcentaje correctamente (3/5 = 60%)")
    void agregarProgreso_calculaPorcentaje() {
        //* Arrange: 3 recursos completados de 5 totales
        AgregarProgreso req = new AgregarProgreso();
        req.setId_usuario(1);
        req.setId_tutorial(1);
        req.setRecursos_completados(3);
        req.setCantidad_recursos_totales(5);
        req.setPreguntas_acertadas(8);
        req.setPreguntas_falladas(2);

        //* Simula que no hay progreso previo para ese usuario+tutorial
        when(progresoRepository.findByIdUsuarioAndIdTutorial(1, 1))
                .thenReturn(Optional.empty());
        when(progresoRepository.save(any(Progreso.class))).thenAnswer(inv -> {
            Progreso p = inv.getArgument(0);
            //* 3/5 = 60% — el servicio calcula y asigna el porcentaje antes de guardar
            assertEquals(60.0, p.getPorcentaje_progreso());
            return progresoBase;
        });

        //* Act
        progresoService.agregarProgreso(req);

        verify(progresoRepository, times(1)).save(any(Progreso.class));
    }

    @Test
    @DisplayName("agregarProgreso: retorna 0% si el total de recursos es 0")
    void agregarProgreso_totalCero_porcentajeCero() {
        //* Arrange: usuario que acaba de registrarse en el tutorial — aún no tiene recursos
        AgregarProgreso req = new AgregarProgreso();
        req.setId_usuario(2);
        req.setId_tutorial(2);
        req.setRecursos_completados(0);
        req.setCantidad_recursos_totales(0);  // total=0 provoca división por cero
        req.setPreguntas_acertadas(0);
        req.setPreguntas_falladas(0);

        when(progresoRepository.findByIdUsuarioAndIdTutorial(2, 2))
                .thenReturn(Optional.empty());
        when(progresoRepository.save(any(Progreso.class))).thenAnswer(inv -> {
            Progreso p = inv.getArgument(0);
            //! División por cero → el servicio debe retornar 0.0, nunca lanzar ArithmeticException
            assertEquals(0.0, p.getPorcentaje_progreso());
            return progresoBase;
        });

        //* Act: no debe lanzar excepción
        progresoService.agregarProgreso(req);
    }

    @Test
    @DisplayName("agregarProgreso: lanza 409 si ya existe un registro para usuario+tutorial")
    void agregarProgreso_duplicado_lanza409() {
        //* Arrange: simula que ya existe un registro para esa combinación
        AgregarProgreso req = new AgregarProgreso();
        req.setId_usuario(1);
        req.setId_tutorial(1);
        req.setRecursos_completados(1);
        req.setCantidad_recursos_totales(5);
        req.setPreguntas_acertadas(1);
        req.setPreguntas_falladas(0);

        //* El repositorio devuelve el progreso existente → se debe rechazar la creación
        when(progresoRepository.findByIdUsuarioAndIdTutorial(1, 1))
                .thenReturn(Optional.of(progresoBase));

        //! HTTP 409 Conflict — un usuario no puede tener dos registros para el mismo tutorial
        assertThrows(ResponseStatusException.class,
                () -> progresoService.agregarProgreso(req));
        //* Si ya existe, nunca debe intentar guardar otro
        verify(progresoRepository, never()).save(any());
    }

    // ── actualizarProgreso ─────────────────────────────────────────────────────

    @Test
    @DisplayName("actualizarProgreso: recalcula porcentaje y actualiza timestamp")
    void actualizarProgreso_recalculaPorcentaje() {
        //* Arrange: el usuario completó todos los recursos — 5/5 = 100%
        ActualizarProgreso req = new ActualizarProgreso();
        req.setRecursos_completados(5);
        req.setCantidad_recursos_totales(5);
        req.setPreguntas_acertadas(10);
        req.setPreguntas_falladas(0);

        when(progresoRepository.findById(1)).thenReturn(Optional.of(progresoBase));
        when(progresoRepository.save(any(Progreso.class))).thenAnswer(inv -> {
            Progreso p = inv.getArgument(0);
            //* 5/5 = 100% — el porcentaje debe recalcularse, no mantenerse el anterior (60%)
            assertEquals(100.0, p.getPorcentaje_progreso());
            //* El timestamp debe actualizarse para reflejar la última actividad
            assertNotNull(p.getFecha_ultima_actividad());
            return p;
        });

        //* Act
        progresoService.actualizarProgreso(1, req);
    }

    // ── eliminarProgreso ───────────────────────────────────────────────────────

    @Test
    @DisplayName("eliminarProgreso: elimina correctamente cuando existe")
    void eliminarProgreso_existente() {
        //* Arrange
        when(progresoRepository.existsById(1)).thenReturn(true);
        doNothing().when(progresoRepository).deleteById(1);

        //* Act
        String resultado = progresoService.eliminarProgreso(1);

        //* Assert
        assertEquals("Progreso eliminado correctamente.", resultado);
        verify(progresoRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("eliminarProgreso: lanza 404 si no existe")
    void eliminarProgreso_noExiste_lanza404() {
        //* Arrange
        when(progresoRepository.existsById(99)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> progresoService.eliminarProgreso(99));
    }

    // ── obtenerProgresoPorUsuarioYTutorial ─────────────────────────────────────

    @Test
    @DisplayName("obtenerProgresoPorUsuarioYTutorial: retorna DTO cuando existe la combinación")
    void obtenerProgresoPorUsuarioYTutorial_existe() {
        //* Arrange
        when(progresoRepository.findByIdUsuarioAndIdTutorial(1, 1))
                .thenReturn(Optional.of(progresoBase));

        //* Act
        ProgresoDTO resultado =
                progresoService.obtenerProgresoPorUsuarioYTutorial(1, 1);

        //* Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.id_tutorial());
    }

    @Test
    @DisplayName("obtenerProgresoPorUsuarioYTutorial: lanza 404 si no hay progreso")
    void obtenerProgresoPorUsuarioYTutorial_noExiste_lanza404() {
        //* Arrange: el usuario 1 no ha iniciado el tutorial 99
        when(progresoRepository.findByIdUsuarioAndIdTutorial(1, 99))
                .thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> progresoService.obtenerProgresoPorUsuarioYTutorial(1, 99));
    }
}
