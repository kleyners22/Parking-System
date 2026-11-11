-- PostgreSQL 14+ (compatible con MySQL cambiando SERIAL/TIMESTAMP segun corresponda)
CREATE TABLE locations (
  id          SERIAL PRIMARY KEY,
  code        VARCHAR(20)  NOT NULL UNIQUE,
  name        VARCHAR(100) NOT NULL,
  address     VARCHAR(200),
  notes       TEXT,
  lot_mobil   INT NOT NULL DEFAULT 0 CHECK (lot_mobil >= 0),  -- espacios autos
  lot_motor   INT NOT NULL DEFAULT 0 CHECK (lot_motor >= 0)   -- espacios motos
);

CREATE TABLE users (
  id            SERIAL PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  email         VARCHAR(120) NOT NULL UNIQUE,
  full_name     VARCHAR(120),
  phone         VARCHAR(30),
  address       VARCHAR(200),
  birth_date    DATE,
  gender        VARCHAR(20),
  photo_url     TEXT,
  role          VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','OPERATOR')),
  active        BOOLEAN NOT NULL DEFAULT TRUE,
  created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE vehicle_types (
  id          SERIAL PRIMARY KEY,
  code        VARCHAR(20) NOT NULL UNIQUE,   -- CAR, MOTO
  type        VARCHAR(50) NOT NULL,
  description VARCHAR(200)
);

CREATE TABLE access_points (
  id              SERIAL PRIMARY KEY,
  code            VARCHAR(20) NOT NULL UNIQUE,
  name            VARCHAR(100) NOT NULL,
  type            VARCHAR(30),                -- ENTRADA / SALIDA
  description     VARCHAR(200),
  vehicle_type_id INT NOT NULL REFERENCES vehicle_types(id),
  created_by      INT NOT NULL REFERENCES users(id)
);

CREATE TABLE tariffs (
  id           SERIAL PRIMARY KEY,
  code         VARCHAR(20) NOT NULL UNIQUE,
  type         VARCHAR(20) NOT NULL CHECK (type IN ('REGULAR','MEMBER','VIP')),
  hourly_value NUMERIC(10,2) NOT NULL CHECK (hourly_value >= 0),
  max_value    NUMERIC(10,2),
  description  VARCHAR(200),
  created_by   INT NOT NULL REFERENCES users(id)
);

CREATE TABLE members (
  id              SERIAL PRIMARY KEY,
  card_number     VARCHAR(30) NOT NULL UNIQUE,
  plate           VARCHAR(15) NOT NULL,
  phone           VARCHAR(30),
  name            VARCHAR(120) NOT NULL,
  category        VARCHAR(20) NOT NULL DEFAULT 'MEMBER' CHECK (category IN ('MEMBER','VIP')),
  vehicle_type_id INT NOT NULL REFERENCES vehicle_types(id),
  location_id     INT NOT NULL REFERENCES locations(id),
  start_date      DATE NOT NULL,
  end_date        DATE NOT NULL CHECK (end_date >= start_date),
  active          BOOLEAN NOT NULL DEFAULT TRUE,
  notes           TEXT,
  user_id         INT UNIQUE REFERENCES users(id),     -- RF-24: 1:0..1
  created_by      INT NOT NULL REFERENCES users(id)
);
CREATE INDEX idx_members_plate ON members(plate);

CREATE TABLE transactions (
  id              BIGSERIAL PRIMARY KEY,
  code            VARCHAR(40) NOT NULL UNIQUE,
  plate           VARCHAR(15) NOT NULL,
  type            VARCHAR(10) NOT NULL CHECK (type IN ('MASUK','KELUAR')),
  status          VARCHAR(12) NOT NULL CHECK (status IN ('BERJALAN','SELESAI','DIBATALKAN')),
  entry_time      TIMESTAMP NOT NULL,
  exit_time       TIMESTAMP,
  entry_ap_id     INT NOT NULL REFERENCES access_points(id),
  exit_ap_id      INT REFERENCES access_points(id),
  location_id     INT NOT NULL REFERENCES locations(id),
  vehicle_type_id INT NOT NULL REFERENCES vehicle_types(id),
  tariff_id       INT REFERENCES tariffs(id),
  member_id       INT REFERENCES members(id),
  entry_image     TEXT,
  exit_image      TEXT,
  duration_min    BIGINT,
  amount          NUMERIC(10,2),
  qr_code         TEXT,                                 -- PNG en Base64
  operator_id     INT NOT NULL REFERENCES users(id)
);
CREATE INDEX idx_tx_open ON transactions(plate, status);
CREATE INDEX idx_tx_loc  ON transactions(location_id, vehicle_type_id, status);

-- Datos semilla (la contrasena debe insertarse ya hasheada con BCrypt desde la app)
INSERT INTO vehicle_types(code,type,description) VALUES ('CAR','Auto','Vehiculo liviano'),('MOTO','Moto','Motocicleta');
