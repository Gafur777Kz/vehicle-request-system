# VehicleRequestSystem

## Product

We are building a request system for company vehicles: an employee requests a car, a manager approves the request, and a dispatcher assigns a driver.

## Core item

Users track a **vehicle request** —it has an identifier (`VehicleRequestId`) and a status (`VehicleRequestStatus`).

Status: `DRAFT`, `SUBMITTED`, `APPROVED`, `ASSIGNED`, `COMPLETED`, `REJECTED`.

## Status table

| From | To | Allowed |
|-----------|-----------|---------|
| DRAFT | SUBMITTED | yes |
| SUBMITTED | APPROVED | yes |
| DRAFT | APPROVED | no |
| COMPLETED | ASSIGNED | no |

## Forbidden — why

| From | To | Почему запрещено |
|-----------|-----------|------------------|
| DRAFT | APPROVED | Пропущено согласование: заявка не была отправлена руководителю, значит машину выдали бы без чьего-либо решения. |
| COMPLETED | ASSIGNED | Поездка уже закрыта. Переназначение машины на закрытую заявку исказило бы отчёт по пробегу и топливу — нужна новая заявка. |

## How to launch

```
java -version   # 21
mvn -q test
```
