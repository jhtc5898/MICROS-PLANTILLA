-- =====================================================
-- Script de Base de Datos - Sistema de Microservicios
-- =====================================================
-- Este script contiene el esquema completo de las bases de datos
-- para el sistema de gestión de clientes y cuentas bancarias.
--
-- Arquitectura:
-- - Customer Service Database: Gestiona información de personas y clientes
-- - Account Service Database: Gestiona cuentas bancarias y movimientos
--
-- Tecnologías: PostgreSQL con R2DBC para acceso reactivo
-- =====================================================

-- =====================================================
-- ESQUEMA: Customer Service Database (customerdb)
-- Puerto: 5432
-- =====================================================
-- Esta base de datos almacena toda la información relacionada
-- con personas físicas y clientes del banco.

-- =====================================================
-- TABLA: persons
-- Descripción: Almacena información básica de personas
-- Propósito: Entidad base para clientes y posibles futuras extensiones
-- =====================================================
-- Columnas principales:
-- - id: Identificador único autoincremental
-- - name: Nombre completo de la persona (requerido)
-- - gender: Género (M/F/O) (requerido)
-- - identification: Número de identificación único (CI/Pasaporte) (requerido)
-- - address: Dirección de residencia (opcional)
-- - phone: Número de teléfono (opcional)
-- - created_at: Fecha de creación del registro
-- - updated_at: Fecha de última actualización
--
-- Restricciones:
-- - UNIQUE(identification): No permite identificaciones duplicadas
-- - NOT NULL en campos críticos
-- =====================================================

CREATE TABLE public.persons (
	id bigserial NOT NULL,
	"name" varchar(255) NOT NULL,
	gender varchar(50) NOT NULL,
	identification varchar(100) NOT NULL,
	address varchar(500) NULL,
	phone varchar(50) NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT persons_identification_key UNIQUE (identification),
	CONSTRAINT persons_pkey PRIMARY KEY (id)
);

-- =====================================================
-- ÍNDICES: persons
-- =====================================================
-- idx_persons_identification: Acelera búsquedas por identificación
-- (usado frecuentemente en validaciones de duplicados)
-- =====================================================
CREATE INDEX idx_persons_identification ON public.persons USING btree (identification);

-- =====================================================
-- TABLA: customers
-- Descripción: Almacena información específica de clientes
-- Propósito: Extiende la tabla persons con datos de cliente
-- Relación: 1 persona puede ser 1 cliente (1:1)
-- =====================================================
-- Columnas principales:
-- - id: Identificador único del cliente
-- - person_id: Referencia a la persona (FK obligatoria)
-- - password: Contraseña encriptada (requerido)
-- - status: Estado del cliente (true=activo, false=inactivo)
-- - created_at: Fecha de creación del cliente
-- - updated_at: Fecha de última actualización
--
-- Restricciones:
-- - FK a persons con CASCADE DELETE: Si se elimina persona, se elimina cliente
-- - status por defecto true
-- =====================================================

CREATE TABLE public.customers (
	id bigserial NOT NULL,
	person_id int8 NOT NULL,
	"password" varchar(255) NOT NULL,
	status bool DEFAULT true NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);

-- =====================================================
-- ÍNDICES: customers
-- =====================================================
-- idx_customers_person_id: Acelera búsquedas por persona
-- (usado en joins entre customers y persons)
-- =====================================================
CREATE INDEX idx_customers_person_id ON public.customers USING btree (person_id);

-- =====================================================
-- RELACIONES EXTERNAS: customers
-- =====================================================
-- FK: customers_person_id_fkey
-- - Referencia: persons(id)
-- - Comportamiento: CASCADE DELETE (elimina cliente si se elimina persona)
-- =====================================================

ALTER TABLE public.customers ADD CONSTRAINT customers_person_id_fkey FOREIGN KEY (person_id) REFERENCES public.persons(id) ON DELETE CASCADE;

-- =====================================================
-- ESQUEMA: Account Service Database (accountdb)
-- Puerto: 5433
-- =====================================================
-- Esta base de datos almacena toda la información relacionada
-- con cuentas bancarias y sus movimientos/transacciones.

-- =====================================================
-- TABLA: accounts
-- Descripción: Almacena información de cuentas bancarias
-- Propósito: Entidad principal para cuentas de ahorro/corriente
-- Relación: 1 cliente puede tener N cuentas (1:N)
-- =====================================================
-- Columnas principales:
-- - id: Identificador único de la cuenta
-- - account_number: Número de cuenta único (requerido)
-- - account_type: Tipo de cuenta (Ahorro/Corriente) (requerido)
-- - initial_balance: Saldo inicial al crear la cuenta (requerido)
-- - current_balance: Saldo actual (calculado dinámicamente)
-- - status: Estado de la cuenta (true=activa, false=inactiva)
-- - customer_id: Referencia al cliente propietario (FK obligatoria)
-- - created_at: Fecha de creación de la cuenta
-- - updated_at: Fecha de última actualización
--
-- Restricciones:
-- - UNIQUE(account_number): No permite números de cuenta duplicados
-- - FK a customers (sin CASCADE: cuentas no se eliminan automáticamente)
-- =====================================================

CREATE TABLE public.accounts (
	id bigserial NOT NULL,
	account_number varchar(50) NOT NULL,
	account_type varchar(50) NOT NULL,
	initial_balance numeric(19, 2) NOT NULL,
	current_balance numeric(19, 2) NOT NULL,
	status bool DEFAULT true NOT NULL,
	customer_id int8 NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT accounts_account_number_key UNIQUE (account_number),
	CONSTRAINT accounts_pkey PRIMARY KEY (id)
);

-- =====================================================
-- ÍNDICES: accounts
-- =====================================================
-- idx_accounts_account_number: Acelera búsquedas por número de cuenta
-- idx_accounts_customer_id: Acelera búsquedas de cuentas por cliente
-- =====================================================
CREATE INDEX idx_accounts_account_number ON public.accounts USING btree (account_number);
CREATE INDEX idx_accounts_customer_id ON public.accounts USING btree (customer_id);

-- =====================================================
-- TABLA: movements
-- Descripción: Almacena todos los movimientos/transacciones
-- Propósito: Registro histórico de débitos y créditos
-- Relación: 1 cuenta puede tener N movimientos (1:N)
-- =====================================================
-- Columnas principales:
-- - id: Identificador único del movimiento
-- - account_id: Referencia a la cuenta (FK obligatoria)
-- - movement_date: Fecha y hora exacta del movimiento
-- - movement_type: Tipo (DEBIT=retiro, CREDIT=depósito)
-- - value: Monto del movimiento (siempre positivo)
-- - balance: Saldo de la cuenta después del movimiento
-- - created_at: Fecha de registro en BD
--
-- Restricciones:
-- - CHECK(movement_type IN ('DEBIT','CREDIT')): Solo tipos válidos
-- - CHECK(value > 0): Monto debe ser positivo
-- - FK a accounts con CASCADE DELETE: Elimina movimientos si se elimina cuenta
-- =====================================================

CREATE TABLE public.movements (
	id bigserial NOT NULL,
	account_id int8 NOT NULL,
	movement_date timestamp NOT NULL,
	movement_type varchar(20) NOT NULL,
	value numeric(19, 2) NOT NULL,
	balance numeric(19, 2) NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT movements_movement_type_check CHECK (((movement_type)::text = ANY ((ARRAY['DEBIT'::character varying, 'CREDIT'::character varying])::text[]))),
	CONSTRAINT movements_pkey PRIMARY KEY (id),
	CONSTRAINT movements_value_check CHECK ((value > (0)::numeric))
);

-- =====================================================
-- ÍNDICES: movements
-- =====================================================
-- idx_movements_account_id: Acelera búsquedas de movimientos por cuenta
-- idx_movements_date: Acelera consultas por rango de fechas
-- =====================================================
CREATE INDEX idx_movements_account_id ON public.movements USING btree (account_id);
CREATE INDEX idx_movements_date ON public.movements USING btree (movement_date);

-- =====================================================
-- RELACIONES EXTERNAS: movements
-- =====================================================
-- FK: movements_account_id_fkey
-- - Referencia: accounts(id)
-- - Comportamiento: CASCADE DELETE (elimina movimientos si se elimina cuenta)
-- =====================================================

ALTER TABLE public.movements ADD CONSTRAINT movements_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(id) ON DELETE CASCADE;

-- =====================================================
-- TABLA: customers
-- Descripción: Almacena el estado de los clientes en account-service
-- Propósito: Mantener respaldo del estado del cliente para activar/desactivar cuentas
-- =====================================================
-- Columnas principales:
-- - id: Identificador único del cliente (igual al customerId del customer-service)
-- - status: Estado del cliente (ACTIVE/INACTIVE)
-- - created_at: Fecha de creación del registro
-- - updated_at: Fecha de última actualización
-- =====================================================

CREATE TABLE public.customers (
	id bigserial NOT NULL,
	status varchar(50) NOT NULL,
	created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
	CONSTRAINT customers_pkey PRIMARY KEY (id)
);

-- =====================================================
-- ÍNDICES: customers
-- =====================================================
-- idx_customers_id: Acelera búsquedas por ID de cliente
-- =====================================================
CREATE INDEX idx_customers_id ON public.customers USING btree (id);

-- =====================================================
-- ÍNDICES ADICIONALES Y OPTIMIZACIONES
-- =====================================================
-- Los siguientes índices están duplicados por claridad,
-- pero en producción solo se crearían una vez.
--
-- Optimizaciones principales:
-- - Índices en campos de búsqueda frecuente
-- - Índices únicos para constraints
-- - Índices compuestos si fueran necesarios
-- =====================================================

-- Índices para Customer Service (ya creados arriba, listado por completitud)
-- CREATE INDEX idx_customers_person_id ON public.customers USING btree (person_id);
-- CREATE INDEX idx_persons_identification ON public.persons USING btree (identification);
-- CREATE UNIQUE INDEX persons_identification_key ON public.persons USING btree (identification);
-- CREATE UNIQUE INDEX customers_pkey ON public.customers USING btree (id);
-- CREATE UNIQUE INDEX persons_pkey ON public.persons USING btree (id);

-- Índices para Account Service (ya creados arriba, listado por completitud)
-- CREATE INDEX idx_accounts_customer_id ON public.accounts USING btree (customer_id);
-- CREATE INDEX idx_accounts_account_number ON public.accounts USING btree (account_number);
-- CREATE INDEX idx_movements_account_id ON public.movements USING btree (account_id);
-- CREATE INDEX idx_movements_date ON public.movements USING btree (movement_date);
-- CREATE INDEX idx_customers_id ON public.customers USING btree (id);

-- =====================================================
-- NOTAS DE USO Y MANTENIMIENTO
-- =====================================================
--
-- 1. Backup regular: Ambas bases de datos deben backuparse juntas
--    por las relaciones cross-service.
--
-- 2. Migraciones: Cualquier cambio en esquema debe ser versionado
--    y probado en ambientes de desarrollo primero.
--
-- 3. Rendimiento: Monitorear uso de índices y agregar si es necesario
--    (ej: índices compuestos para consultas complejas).
--
-- 4. Seguridad: Las contraseñas deben estar encriptadas.
--    Campos sensibles deben auditarse.
--
-- 5. Datos de prueba: Ver archivos de documentación para ejemplos
--    de datos de prueba.
--
-- =====================================================
-- FIN DEL SCRIPT
-- =====================================================
