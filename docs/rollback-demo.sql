-- Week 5: доказать откат руками в psql, до Java.
-- Запуск: psql -h localhost -U css -d css -f docs/rollback-demo.sql
-- (ON_ERROR_STOP выключен, чтобы после ошибки дойти до ROLLBACK)

DROP TABLE IF EXISTS vehicle_request;
\i src/main/resources/db/schema.sql

BEGIN;
INSERT INTO vehicle_request (id, business_key, status, purpose)
VALUES ('11111111-1111-1111-1111-111111111111',
        'VR-1042', 'DRAFT', 'Поездка в аэропорт');
INSERT INTO vehicle_request (id, business_key, status, purpose)
VALUES ('22222222-2222-2222-2222-222222222222',
        'VR-1042', 'DRAFT', 'Поездка в аэропорт ещё раз');
-- ERROR: duplicate key value violates unique constraint
ROLLBACK;

SELECT count(*) FROM vehicle_request WHERE business_key = 'VR-1042';
-- 0
