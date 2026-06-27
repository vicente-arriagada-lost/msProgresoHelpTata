# ms-Progreso

Microservicio de seguimiento del avance de los usuarios de HelpTata. Registra el porcentaje de progreso de cada usuario en cada tutorial, permitiendo al frontend mostrar estadísticas personales y motivar la continuación de los cursos.

---

## Tecnologías

| Herramienta | Versión | Para qué se usa |
|---|---|---|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3 | Framework de aplicación |
| Spring Data JPA | — | Acceso a base de datos |
| PostgreSQL | 16 | Base de datos relacional |
| Maven | 3.9 | Gestor de dependencias |

---

## Requisitos previos

- Java 21 instalado (`java -version`)
- PostgreSQL corriendo en el puerto 5432
- Base de datos `helptata_progreso` creada

```sql
CREATE DATABASE helptata_progreso;
```

---

## Configuración

```properties
server.port=8083
spring.datasource.url=jdbc:postgresql://localhost:5432/helptata_progreso
spring.datasource.username=postgres
spring.datasource.password=
```

---

## Ejecución

```bash
cd msProgresoHelpTata
./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8083`.

---

## Endpoints disponibles

### Progreso — `/api/progreso`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/progreso` | Lista todos los registros de progreso |
| GET | `/api/progreso/{id}` | Obtiene un registro por ID |
| GET | `/api/progreso/usuario/{idUsuario}` | Progreso de un usuario en todos sus cursos |
| GET | `/api/progreso/tutorial/{idTutorial}` | Progreso de todos los usuarios en un tutorial |
| GET | `/api/progreso/usuario/{idUsuario}/tutorial/{idTutorial}` | Progreso de un usuario en un tutorial específico |
| POST | `/api/progreso` | Crea un nuevo registro de progreso |
| PUT | `/api/progreso/{id}` | Actualiza el porcentaje de progreso |
| DELETE | `/api/progreso/{id}` | Elimina un registro |

---

## Ejemplo de respuesta — Progreso de un usuario

```bash
curl http://localhost:8083/api/progreso/usuario/1
```

```json
[
  {
    "id_progreso": 1,
    "id_usuario": 1,
    "id_tutor": 1,
    "porcentaje_progreso": 85
  },
  {
    "id_progreso": 2,
    "id_usuario": 1,
    "id_tutor": 2,
    "porcentaje_progreso": 40
  }
]
```

Un usuario aprueba un curso cuando `porcentaje_progreso >= 70`.

---

## Despliegue en producción

En producción este microservicio corre en un contenedor Docker. No es necesario instalar Java ni Maven en el servidor.

```bash
docker compose up --build ms-progreso -d
```

La URL de la base de datos se inyecta mediante `SPRING_DATASOURCE_URL` definida en el `docker-compose.yml`.

---

## Integración con ms-Logs

Este servicio reporta eventos a `ms-Logs` (puerto 8081) de forma automática y fire-and-forget:

| Evento | Tipo de log |
|---|---|
| Progreso iniciado (primer registro usuario+tutorial) | `INFO` |
| Progreso actualizado | `INFO` |
| Cualquier excepción no manejada | `ERROR` (vía `GlobalExceptionHandler`) |
| `ResponseStatusException` 4xx | `WARNING` (vía `GlobalExceptionHandler`) |

---

## Pruebas unitarias

Las pruebas se ubican en `src/test/java/com/Progreso/ms/ProgresoServiceTest.java` y usan **JUnit 5 + Mockito**. No se necesita base de datos.

**Ejecutar:**
```bash
cd msProgresoHelpTata
./mvnw test
```

| Test | Qué verifica |
|---|---|
| `obtenerTodosLosProgresos_retornaLista` | Lista de progresos correcta |
| `obtenerProgresoPorId_existente` | DTO correcto para ID existente |
| `obtenerProgresoPorId_noExiste_lanza404` | HTTP 404 para ID inexistente |
| `agregarProgreso_calculaPorcentaje` | 3/5 recursos = 60% calculado correctamente |
| `agregarProgreso_totalCero_porcentajeCero` | División por cero retorna 0%, no lanza excepción |
| `agregarProgreso_duplicado_lanza409` | HTTP 409 si ya existe el progreso para usuario+tutorial |
| `actualizarProgreso_recalculaPorcentaje` | Porcentaje recalculado y timestamp actualizado |
| `eliminarProgreso_existente` | Eliminación exitosa |
| `eliminarProgreso_noExiste_lanza404` | HTTP 404 al eliminar ID inexistente |
| `obtenerProgresoPorUsuarioYTutorial_existe` | DTO correcto para combinación usuario+tutorial |
| `obtenerProgresoPorUsuarioYTutorial_noExiste_lanza404` | HTTP 404 si no hay progreso para esa combinación |
