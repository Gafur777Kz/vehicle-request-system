# Vehicle Request System

Система заявок на служебные машины: сотрудник создаёт заявку, руководитель согласует, диспетчер назначает машину.

## Статусы

`DRAFT` → `SUBMITTED` → `APPROVED` → `ASSIGNED` → `COMPLETED`

Из `SUBMITTED` можно в `REJECTED`.

## Правила (Lab 2)

- `TransitionRule` — проверяет таблицу статусов (например, нельзя `DRAFT` → `APPROVED`).
- `ClosedRequestRule` — из `COMPLETED` и `REJECTED` никуда перейти нельзя.

Оба реализуют интерфейс `Rule`. `VehicleRequestService` получает `Rule` через Spring.

## Пакеты

```
  dto          client        handler         config
  (JSON later) (HTTP later)  (HTTP week 9)   Application
                                             VehicleRequestService
       \            \            /                |
        \            \          /           injects Rule
         \            \        /
                     domain
     VehicleRequestId  VehicleRequestStatus  VehicleRequestPolicy
     Rule  TransitionRule  ClosedRequestRule
                   (no Spring)
```

В `domain` нет Spring.

## Запуск

```
mvn -q verify
mvn spring-boot:run
```
