# Vehicle Request System

## Product

Мы делаем систему заявок на служебные машины. Люди отслеживают заявку на машину (vehicle request).

Сотрудник создаёт заявку, руководитель согласует, диспетчер назначает машину.

## Core item

Заявка на машину — `VehicleRequest`.

- `VehicleRequestId` — UUID, колонка `id`
- номер для человека `VR-1042` — колонка `business_key`
- `VehicleRequestStatus` — статусы ниже
- `VehicleRequestPolicy` — какие переходы разрешены

Статусы: `DRAFT` → `SUBMITTED` → `APPROVED` → `ASSIGNED` → `COMPLETED`, из `SUBMITTED` можно в `REJECTED`.

## Status table

| Из | В | Результат |
| --- | --- | --- |
| `DRAFT` | `SUBMITTED` | разрешено |
| `SUBMITTED` | `APPROVED` | разрешено |
| `DRAFT` | `APPROVED` | запрещено |
| `COMPLETED` | `DRAFT` | запрещено |

Эти четыре строки проверяются в `VehicleRequestPolicyTest` через `@CsvSource`.

## Forbidden — why

- `DRAFT` → `APPROVED`: нельзя пропустить отправку на согласование, руководитель не видел заявку.
- `COMPLETED` → `DRAFT`: поездка уже была, машину вернули. Нужно создать новую заявку.

## Rules (Lab 2)

Интерфейс `Rule`, две реализации:

- `TransitionRule` — проверяет таблицу переходов через `VehicleRequestPolicy`
- `ClosedRequestRule` — из `COMPLETED` и `REJECTED` никуда нельзя

Они соединяются в `RuleConfig` (`@Bean`), `VehicleRequestService` получает `Rule` через конструктор.

## Packages

```
  dto            client                handler        config
  (JSON later)   LogNotificationClient (HTTP week 9)  Application, RuleConfig
                                                      VehicleRequestService (@Transactional)
       \              \                  /                  |               \
        \              \                /              injects Rule      persistence
                          domain                                         VehicleRequestEntity (@Entity)
     VehicleRequestId  VehicleRequestStatus  VehicleRequestPolicy        VehicleRequestJpa (EntityManager)
     Rule  TransitionRule  ClosedRequestRule
     NotificationPort  DuplicateVehicleRequest
              (no Spring, no JPA)
```

## Database (Lab 3)

Таблица создаётся миграцией Flyway `src/main/resources/db/migration/V1__create_vehicle_request.sql`.
Hibernate таблицы не создаёт, только проверяет (`ddl-auto: validate`).

- `id uuid PRIMARY KEY` — суррогатный ключ
- `business_key text NOT NULL UNIQUE` — номер заявки
- `status text NOT NULL` + `CHECK` с теми же шестью статусами
- `purpose text NOT NULL` — цель поездки
- `created_at timestamptz DEFAULT now()`

Тесты (`VehicleRequestJpaTest`):

- `secondStatementRollsBack` — `insertTwice`, одна транзакция, второй insert падает → откат, count = **0**
- `secondRequestKeepsTheFirst` — второй `register` с тем же номером → `DuplicateVehicleRequest`, первая заявка остаётся, count = **1**

`DuplicateVehicleRequest` — unchecked, лежит в `domain`. Ловим `DataIntegrityViolationException` после `flush()` в сервисе.

Откат руками в psql: `docs/rollback-demo.sql`.

## Run

Нужен PostgreSQL на `localhost:5432`, база `css`, пользователь `css` / `css` (учебный дефолт).

```
docker run -d --name css-pg -p 5432:5432 -e POSTGRES_USER=css -e POSTGRES_PASSWORD=css -e POSTGRES_DB=css postgres:16
```

Если в базе уже есть таблица от старого JDBC-варианта, Flyway откажется запускать V1. Пересоздать базу (подключиться к `postgres`, не к `css`):

```
DROP DATABASE IF EXISTS css WITH (FORCE);
CREATE DATABASE css OWNER css;
```

```
mvn -q verify
mvn spring-boot:run
```
