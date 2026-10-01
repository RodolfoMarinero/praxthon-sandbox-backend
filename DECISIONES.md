# Bitácora de decisiones

- La Guía v3.0 define el mapeo V01-V19 a códigos PRX.
- Las validaciones sintácticas se acumulan antes de persistir.
- La resolución de escenarios y la máquina de estados permanecen fuera del controlador.
- LIQUIDADO, DEVUELTO y RECHAZADO son terminales.
- El alta persiste y responde RECIBIDO; el desenlace se procesa después del commit.
- La idempotencia tiene una restricción única en SQL Server y conserva el hash canónico del cuerpo.
- Las transiciones se guardan en una tabla separada para reconstruir el historial.
- Se incluyen 120 operaciones semilla para demostrar paginación y estados.
