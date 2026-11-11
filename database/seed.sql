-- Datos iniciales para poder operar el sistema.
-- Ejecutar DESPUES de schema.sql:
--   psql -h localhost -U parking -d parking -f database/seed.sql
-- Usuario admin por defecto: admin / admin123

INSERT INTO users(username,password_hash,email,full_name,role,active)
VALUES ('admin','$2a$10$SQLHMsL/gxlNZs4mqN.Cb.jjXgH2AauGRGPQqXT2fT4hAlAadi562','admin@parking.local','Administrador','ADMIN',true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO locations(code,name,address,lot_mobil,lot_motor)
VALUES ('L1','Estacionamiento Central','Av. Principal 123',10,10)
ON CONFLICT (code) DO NOTHING;

INSERT INTO access_points(code,name,type,vehicle_type_id,created_by) VALUES
 ('ENT-CAR','Entrada Autos','ENTRADA',(SELECT id FROM vehicle_types WHERE code='CAR'),(SELECT id FROM users WHERE username='admin')),
 ('SAL-CAR','Salida Autos','SALIDA',(SELECT id FROM vehicle_types WHERE code='CAR'),(SELECT id FROM users WHERE username='admin')),
 ('ENT-MOTO','Entrada Motos','ENTRADA',(SELECT id FROM vehicle_types WHERE code='MOTO'),(SELECT id FROM users WHERE username='admin')),
 ('SAL-MOTO','Salida Motos','SALIDA',(SELECT id FROM vehicle_types WHERE code='MOTO'),(SELECT id FROM users WHERE username='admin'))
ON CONFLICT (code) DO NOTHING;

INSERT INTO tariffs(code,type,hourly_value,max_value,description,created_by) VALUES
 ('REG','REGULAR',2.00,20.00,'Tarifa regular',(SELECT id FROM users WHERE username='admin')),
 ('MEM','MEMBER',1.50,15.00,'Tarifa miembro',(SELECT id FROM users WHERE username='admin')),
 ('VIP','VIP',1.00,10.00,'Tarifa VIP',(SELECT id FROM users WHERE username='admin'))
ON CONFLICT (code) DO NOTHING;
