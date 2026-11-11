# Sistema Web de Gestión y Monitoreo de Estacionamientos

Estructura
- `database/schema.sql`  -> DDL PostgreSQL (tablas, FKs, CHECK como enumeraciones)
- `backend/`             -> Spring Boot 3 / Java 17 (JPA, Security, ZXing)
  - `entity/` modelos JPA · `repo/` Spring Data · `service/` lógica de negocio
  - `security/` RBAC · `web/` controladores REST
  - `resources/static/`   -> frontend (Bootstrap 5 + Chart.js + JS vanilla), servido por Spring

Puesta en marcha
1. `createdb parking && psql parking -f database/schema.sql`
2. `psql parking -f database/seed.sql` -> crea el admin (`admin` / `admin123`), 1 ubicación,
   los puntos de acceso ENTRADA/SALIDA por tipo de vehículo y las tarifas REGULAR, MEMBER y VIP.
3. `cd backend && mvn spring-boot:run` -> http://localhost:8080

Nota: para cambiar la contraseña del admin genere otro hash con
`new BCryptPasswordEncoder().encode("tu-clave")`.

Mapeo de enumeraciones: BERJALAN=En curso, SELESAI=Finalizada, DIBATALKAN=Cancelada,
MASUK=Entrada, KELUAR=Salida.
