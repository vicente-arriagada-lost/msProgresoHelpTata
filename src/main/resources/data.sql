-- ============================================================
-- ms-Progreso: datos de prueba
-- ============================================================

INSERT INTO progreso (id_progreso, id_usuario, id_tutorial, recursos_completados, cantidad_recursos_totales, preguntas_acertadas, preguntas_falladas, porcentaje_progreso, fecha_ultima_actividad)
VALUES
  (1, 1, 1, 8, 10, 4, 1, 80.0, '2024-01-13 09:00:00'),
  (2, 1, 2, 5, 10, 2, 2, 50.0, '2024-01-14 11:00:00'),
  (3, 2, 1, 3, 10, 3, 0, 30.0, '2024-01-15 10:00:00')
ON CONFLICT (id_progreso) DO NOTHING;

SELECT setval(pg_get_serial_sequence('progreso', 'id_progreso'), COALESCE((SELECT MAX(id_progreso) FROM progreso), 1));
