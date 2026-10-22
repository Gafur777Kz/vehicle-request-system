package kz.narxoz.vrs.domain;

// исходящий порт, в тестах мокается
public interface NotificationPort {

    void statusChanged(VehicleRequestId id, VehicleRequestStatus from, VehicleRequestStatus to);
}
