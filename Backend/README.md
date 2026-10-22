# Café Valle Blanco — API Backend

Spring Boot 3.3.13, Java 21, PostgreSQL. Backend REST de referencia para integración con Angular.

## Instalación

1. En `cafe_valle_blanco` ejecutar `Backend/sql/01_schema.sql` desde psql (`\i 'C:/ruta/.../Backend/sql/01_schema.sql'`) o Query Tool de pgAdmin. **Hacer copia de seguridad si existen tablas previas:** `IF NOT EXISTS` no reconcilia esquemas distintos.
2. En PowerShell dentro de Backend: `$env:DB_PASSWORD = 'CONTRASEÑA_LOCAL'` (no poner credenciales en Git); opcional `$env:DB_USERNAME = 'postgres'`.
3. `mvn spring-boot:run` y visitar `http://localhost:8080/api/health`.
4. FRONTEND_ORIGIN por defecto `http://localhost:4200`; configurable mediante variable de entorno.

## Endpoints (JSON)

- `GET /api/health`
- `GET /api/categorias`
- `GET /api/productos?categoria=1&buscar=cafe`
- `GET /api/productos/destacados`
- `GET /api/productos/{id}`
- `POST /api/auth/registro` `{ "nombre":"Ana", "email":"ana@example.org", "password":"ClaveSegura2026" }`
- `POST /api/auth/login` `{ "email":"ana@example.org", "password":"ClaveSegura2026" }` => token Bearer durante 24 h
- `GET /api/auth/me`, `POST /api/auth/logout` (Bearer)
- `GET /api/carrito`, `POST /api/carrito` `{ "productoId":1,"cantidad":2 }`, `PUT /api/carrito/{productoId}` `{ "cantidad":3 }`, `DELETE /api/carrito/{productoId}` (Bearer)
- `POST /api/pedidos` `{ "metodoPago":"YAPE", "direccionEnvio":"Dirección del cliente" }` (Bearer); `GET /api/pedidos` (Bearer)
- **Solo ADMIN:** `POST /api/categorias` `{ "nombre":"Molido", "descripcion":"..." }`; `POST /api/productos` y `PUT /api/productos/{id}` con `categoriaId,nombre,descripcion,precio,stock,tipoPreparado,imagenUrl,destacado,estado`.

## Primer administrador

Para probar endpoints administrativos, crear un usuario por `/api/auth/registro` y luego desde psql, en ambiente local y solo con autorización del equipo, ejecutar `UPDATE app_usuario SET rol='ADMIN' WHERE email='correo-del-administrador';`. No hay un administrador ni contraseña predeterminados.

## Reglas

- Precios positivos, stock >=0, categoría existente, nombres no vacíos; filtros y catálogo solo productos activos.
- El stock se vuelve a comprobar con bloqueo de fila en checkout; total y precios se calculan en el servidor, no se confía en el cliente.
- Contraseñas BCrypt, sesiones aleatorias con SHA-256 almacenado (no JWT), expiración 24 h; no publicar tokens.
- Yape/Plin/transferencia: el pedido queda `PENDIENTE_PAGO`; **NO verifica pagos ni envía dinero**. Un sistema productivo necesita comprobación de pagos y procesos administrativos.
- No contiene imágenes ni datos de café reales. Insertar categorías y productos desde SQL o endpoints ADMIN.

## Nota de integración

Los nombres de campos y rutas se definieron para la API. El frontend actual todavía debe consumirla usando `HttpClient` y adaptar sus interfaces. No se modificó el frontend.

## Antes de producción

Añadir pruebas automatizadas con BD de test, migraciones versionadas, rate limiting, política de contraseñas, recuperación de cuenta, protección ante login por fuerza bruta, paginación, validación de URL de imágenes, pagos verificados, envíos, logout/revocación global, HTTPS y revisión de seguridad.
