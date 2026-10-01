# Praxthon Sandbox 2026

Implementación completa conforme al contrato OpenAPI y a la Guía del Participante v3.0.

## Arranque desde cero

1. Instale Java 21, Maven 3.9+ y SQL Server.
2. Ejecute `database/recreate-database.sql` en SQL Server Management Studio.
3. Configure `DB_USERNAME` y `DB_PASSWORD`.
4. Ejecute `mvn clean test`.
5. Ejecute `mvn spring-boot:run`.
6. Abra `http://localhost:8080/swagger-ui.html`.

## Recursos

- Salud: `GET /api/v1/salud`
- Instituciones: `GET /api/v1/catalogos/instituciones`
- Alta: `POST /api/v1/operaciones`
- Consulta: `GET /api/v1/operaciones/{id}`
- Listado: `GET /api/v1/operaciones?pagina=0&tamano=20`

## Datos semilla

Flyway carga 120 operaciones distribuidas entre LIQUIDADO, DEVUELTO, RECHAZADO y EN_PROCESO. Esto permite demostrar estados terminales y paginación desde el primer arranque.

## Comportamiento asíncrono

El POST responde `201` con estado `RECIBIDO`. Después del commit, el motor procesa el escenario. El GET posterior muestra el estado final.

## Postman

Importe los dos archivos de la carpeta `postman`. Use Postman Desktop o Desktop Agent para acceder a `127.0.0.1`.

## Regla Flyway

No modifique una migración ya aplicada. Si parte de versiones anteriores del proyecto, ejecute el script de recreación de base una vez.
