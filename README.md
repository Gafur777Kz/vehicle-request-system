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
  (JSON later)   LogNotificationClient  Controller    Application, RuleConfig
                 DownstreamProperties   ApiErrorHandler
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

## REST API (Lab 4)

| Запрос | Ответ |
| --- | --- |
| `POST /api/v1/vehicle-requests` `{"businessKey":"VR-1042","purpose":"Поездка в аэропорт"}` | **201**, `Location: .../api/v1/vehicle-requests/{uuid}`, статус `DRAFT` |
| `GET /api/v1/vehicle-requests/{uuid}` | **200**, заявка |
| пустой `purpose` / `businessKey`, `purpose` > 120 | **400** `VALIDATION`, в тексте имя поля |
| `GET /api/v1/vehicle-requests/VR-1042` (номер вместо UUID) | **400** `VALIDATION`, `id must be a UUID` |
| UUID нормальный, но заявки нет | **404** `NOT_FOUND` |
| тот же `businessKey` второй раз | **409** `DUPLICATE`, в базе так и остаётся 1 строка |

`purpose` — это поле `title` из задания (цель поездки, до 120 символов).

Любая ошибка — только `{"code","message","correlationId"}`. Если клиент прислал `X-Correlation-Id`, он возвращается в ответе.

Кто за что отвечает:

- `domain` — `VehicleRequest`, `VehicleRequestNotFound`, `DuplicateVehicleRequest`. Без Spring и без `jakarta.validation`.
- `handler` — `VehicleRequestController` (выбирает 201), `CreateVehicleRequest` (`@NotBlank`, `@Size`), `VehicleRequestResponse`, `ApiError`, `ApiErrorHandler` (исключение → 400/404/409/415/500).
- `persistence` — `VehicleRequestEntity`, в JSON никогда не попадает.

Swagger: `http://localhost:8080/swagger-ui.html`, спецификация `/v3/api-docs`.

## Config (Lab 4, неделя 10)

- `application.yaml` — общее: `ddl-auto: validate`, Flyway, стектрейсы выключены, наружу только `health` и `info`. Паролей нет.
- `application-dev.yaml` — localhost `css`/`css`, `app.downstream.base-url` = порт 9090, `timeout: 2s`. Включается по умолчанию.
- `application-prod.yaml` — `CSS_DB_URL`, `CSS_DB_USER`, `CSS_DB_PASSWORD` из окружения, запуск с `SPRING_PROFILES_ACTIVE=prod`.
- `DownstreamProperties` (`@ConfigurationProperties("app.downstream")`) — адрес соседа и timeout. В сеть пока не ходим, это Lab 6.

`GET /actuator/health` → 200 `UP`. `/actuator/env`, `/actuator/heapdump` → 404.

Тесты:

- `VehicleRequestControllerTest` (`@WebMvcTest`, сервис замокан, без базы) — 201, 400, 404, 409, UUID в пути, `X-Correlation-Id`.
- `PlatformTest` (`@SpringBootTest`, профиль `dev`) — значения из yaml, health 200, env 404, второй POST → 409 и count = 1.

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

Проверка руками:

```
curl -i -X POST localhost:8080/api/v1/vehicle-requests -H "Content-Type: application/json" -d "{\"businessKey\":\"VR-1042\",\"purpose\":\"Поездка в аэропорт\"}"
curl -i localhost:8080/actuator/health
```
