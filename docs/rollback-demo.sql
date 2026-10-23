-- Week 5: проверка отката руками в psql.
-- Таблицу уже создал Flyway (V1), просто запустить приложение один раз.
-- psql -h localhost -U css -d css -f docs/rollback-demo.sql

BEGIN;
INSERT INTO vehicle_request (id, business_key, status, purpose)
VALUES ('11111111-1111-1111-1111-111111111111', 'VR-1042', 'DRAFT', 'Поездка в аэропорт');
INSERT INTO vehicle_request (id, business_key, status, purpose)
VALUES ('22222222-2222-2222-2222-222222222222', 'VR-1042', 'DRAFT', 'Ещё раз');
-- ERROR: duplicate key value violates unique constraint
ROLLBACK;

SELECT count(*) FROM vehicle_request WHERE business_key = 'VR-1042';
-- 0
