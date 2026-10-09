# Vehicle Request System

Система заявок на служебные машины: сотрудник создаёт заявку, руководитель согласует, диспетчер назначает машину.

## Статусы

`DRAFT` → `SUBMITTED` → `APPROVED` → `ASSIGNED` → `COMPLETED`

Из `SUBMITTED` можно в `REJECTED`.

### Четыре контрольных перехода (week 4, `@CsvSource`)

| Из | В | Результат |
| --- | --- | --- |
| `DRAFT` | `SUBMITTED` | разрешено |
| `SUBMITTED` | `APPROVED` | разрешено |
| `DRAFT` | `APPROVED` | запрещено → `IllegalStateException` |
| `COMPLETED` | `DRAFT` | запрещено → `IllegalStateException` |

## Правила (Lab 2)

- `TransitionRule` — проверяет таблицу статусов (например, нельзя `DRAFT` → `APPROVED`).
- `ClosedRequestRule` — из `COMPLETED` и `REJECTED` никуда перейти нельзя.

Оба реализуют интерфейс `Rule`. `VehicleRequestService` получает `Rule` через Spring.

## База данных (Lab 3)

Одна таблица `vehicle_request`, написана руками в `src/main/resources/db/schema.sql`.

| Колонка | Что это |
| --- | --- |
| `id uuid PRIMARY KEY` | суррогатный ключ, `VehicleRequestId.newId()` |
| `business_key text NOT NULL UNIQUE` | номер для человека, `VR-1042` (`VehicleRequestNumber`) |
| `status text NOT NULL` + `CHECK` | шесть статусов выше, ровно как в `VehicleRequestStatus` |
| `purpose text NOT NULL` | цель поездки |
| `created_at timestamptz` | `DEFAULT now()` |

- Повторный номер → `DuplicateVehicleRequest` (unchecked, лежит в `domain`).
- `insertTwice` — одна транзакция, второй `INSERT` падает → откат, count = **0**.
- Второй `register` того же номера → исключение, первая заявка остаётся, count = **1**.
- Только JDBC: ORM, генераторов схемы и REST здесь нет.

Откат руками в psql: `docs/rollback-demo.sql`.

## Пакеты

```
  dto          client              handler         config
  (JSON later) LogNotification     (HTTP week 9)   Application
               Client                              VehicleRequestService (@Transactional)
       \            \                 /                |              \
        \            \               /           injects Rule      persistence
         \            \             /           NotificationPort   VehicleRequestJdbc (JdbcTemplate, ?)
                         domain                                     StatusColumn (enum <-> text)
     VehicleRequestId  VehicleRequestNumber  VehicleRequestStatus
     VehicleRequestPolicy  Rule  TransitionRule  ClosedRequestRule
     NotificationPort  DuplicateVehicleRequest
                   (no Spring, no java.sql)
```

## Запуск

Нужен локальный PostgreSQL: база `css`, пользователь `css` / `css` на `localhost:5432`
(учебный дефолт ноутбука, не секрет).

```
createuser -U postgres -P css        # пароль css
createdb   -U postgres -O css css
```

или только Postgres в Docker:

```
docker run -d --name css-pg -p 5432:5432 -e POSTGRES_USER=css -e POSTGRES_PASSWORD=css -e POSTGRES_DB=css postgres:16
```

Тесты сами пересоздают таблицу из `schema.sql`.

```
mvn -q verify
mvn spring-boot:run
```
