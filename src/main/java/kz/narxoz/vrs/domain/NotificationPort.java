package kz.narxoz.vrs.domain;

/**
 * Исходящий порт: сообщить сотруднику, что заявка сменила статус.
 * Реальная отправка (почта/HTTP) появится позже; в тестах порт мокается.
 */
public interface NotificationPort {

    void statusChanged(VehicleRequestId id, VehicleRequestStatus from, VehicleRequestStatus to);
}
