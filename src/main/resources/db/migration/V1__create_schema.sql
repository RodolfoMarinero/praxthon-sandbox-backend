CREATE TABLE instituciones (
    codigo VARCHAR(3) NOT NULL PRIMARY KEY,
    nombre NVARCHAR(100) NOT NULL,
    permite_emision BIT NOT NULL,
    disponible BIT NOT NULL
);

CREATE TABLE operaciones (
    id VARCHAR(29) NOT NULL PRIMARY KEY,
    referencia_seguimiento VARCHAR(30) NOT NULL UNIQUE,
    tipo_operacion VARCHAR(3) NOT NULL,
    importe_valor DECIMAL(19, 2) NOT NULL,
    importe_divisa VARCHAR(3) NOT NULL,
    emisor_institucion VARCHAR(3) NOT NULL,
    emisor_cuenta VARCHAR(18) NULL,
    emisor_sucursal VARCHAR(20) NULL,
    emisor_nombre NVARCHAR(40) NOT NULL,
    emisor_identificacion_fiscal VARCHAR(30) NULL,
    emisor_documento_tipo VARCHAR(20) NULL,
    emisor_documento_numero VARCHAR(50) NULL,
    receptor_institucion VARCHAR(3) NOT NULL,
    receptor_cuenta VARCHAR(18) NOT NULL,
    receptor_nombre NVARCHAR(40) NOT NULL,
    concepto NVARCHAR(40) NOT NULL,
    folio_numerico INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    escenario VARCHAR(3) NOT NULL,
    fecha_registro DATETIMEOFFSET(6) NOT NULL,
    payload_hash VARCHAR(64) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE transiciones_operacion (
    id BIGINT IDENTITY(1, 1) NOT NULL PRIMARY KEY,
    operacion_id VARCHAR(29) NOT NULL REFERENCES operaciones(id),
    estado VARCHAR(20) NOT NULL,
    momento DATETIMEOFFSET(6) NOT NULL,
    motivo VARCHAR(20) NULL,
    tipo_devolucion VARCHAR(20) NULL,
    actor_solicitante VARCHAR(30) NULL
);

CREATE TABLE idempotencias (
    id BIGINT IDENTITY(1, 1) NOT NULL PRIMARY KEY,
    clave_idempotencia VARCHAR(36) NOT NULL UNIQUE,
    payload_hash VARCHAR(64) NOT NULL,
    operacion_id VARCHAR(29) NOT NULL UNIQUE REFERENCES operaciones(id),
    fecha_creacion DATETIMEOFFSET(6) NOT NULL,
    fecha_expiracion DATETIMEOFFSET(6) NOT NULL
);

CREATE INDEX ix_operaciones_fecha
    ON operaciones(fecha_registro DESC);

CREATE INDEX ix_transiciones_operacion
    ON transiciones_operacion(operacion_id, momento);

INSERT INTO instituciones VALUES
    ('801', N'Banco Praxis Alfa', 1, 1),
    ('802', N'Banco Praxis Beta', 1, 1),
    ('803', N'Banco Praxis Gamma', 1, 1),
    ('804', N'Praxis Servicios de Pago', 0, 1),
    ('805', N'Banco Praxis Delta', 1, 0);
