-- Ejecutar en la base cafe_valle_blanco; conserva las tablas existentes si son compatibles.
CREATE TABLE
    IF NOT EXISTS categoria (
        id_categoria BIGSERIAL PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL UNIQUE,
        descripcion VARCHAR(255)
    );

CREATE TABLE
    IF NOT EXISTS producto (
        id_producto BIGSERIAL PRIMARY KEY,
        id_categoria BIGINT NOT NULL REFERENCES categoria (id_categoria),
        nombre VARCHAR(150) NOT NULL,
        descripcion TEXT,
        precio NUMERIC(10, 2) NOT NULL CHECK (precio > 0),
        stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
        tipo_preparado VARCHAR(100),
        imagen_url TEXT,
        destacado BOOLEAN NOT NULL DEFAULT FALSE,
        estado BOOLEAN NOT NULL DEFAULT TRUE,
        CONSTRAINT chk_producto_nombre CHECK (length (trim(nombre)) > 0)
    );

CREATE TABLE
    IF NOT EXISTS app_usuario (
        id BIGSERIAL PRIMARY KEY,
        nombre VARCHAR(120) NOT NULL,
        email VARCHAR(254) NOT NULL UNIQUE,
        password_hash VARCHAR(100) NOT NULL,
        rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE',
        activo BOOLEAN NOT NULL DEFAULT TRUE,
        creado_en TIMESTAMPTZ NOT NULL DEFAULT now ()
    );

CREATE TABLE
    IF NOT EXISTS app_sesion (
        token_hash VARCHAR(64) PRIMARY KEY,
        usuario_id BIGINT NOT NULL REFERENCES app_usuario (id),
        expira_en TIMESTAMPTZ NOT NULL
    );

CREATE TABLE
    IF NOT EXISTS app_carrito_item (
        id BIGSERIAL PRIMARY KEY,
        usuario_id BIGINT NOT NULL REFERENCES app_usuario (id),
        producto_id BIGINT NOT NULL REFERENCES producto (id_producto),
        cantidad INTEGER NOT NULL CHECK (cantidad > 0),
        UNIQUE (usuario_id, producto_id)
    );

CREATE TABLE
    IF NOT EXISTS app_pedido (
        id BIGSERIAL PRIMARY KEY,
        usuario_id BIGINT NOT NULL REFERENCES app_usuario (id),
        creado_en TIMESTAMPTZ NOT NULL DEFAULT now (),
        estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_PAGO',
        metodo_pago VARCHAR(25) NOT NULL,
        direccion_envio VARCHAR(500) NOT NULL,
        total NUMERIC(12, 2) NOT NULL
    );

CREATE TABLE
    IF NOT EXISTS app_pedido_detalle (
        id BIGSERIAL PRIMARY KEY,
        pedido_id BIGINT NOT NULL REFERENCES app_pedido (id),
        producto_id BIGINT NOT NULL REFERENCES producto (id_producto),
        cantidad INTEGER NOT NULL CHECK (cantidad > 0),
        precio_unitario NUMERIC(10, 2) NOT NULL,
        subtotal NUMERIC(12, 2) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto (id_categoria);

CREATE INDEX IF NOT EXISTS idx_pedido_usuario ON app_pedido (usuario_id);