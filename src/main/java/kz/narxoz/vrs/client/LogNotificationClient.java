package kz.narxoz.vrs.client;

import kz.narxoz.vrs.domain.NotificationPort;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.springframework.stereotype.Component;

/** Пока просто пишет в лог. HTTP-клиент заменит его в следующих лабах. */
@Component
public class LogNotificationClient implements NotificationPort {

    @Override
    public void statusChanged(VehicleRequestId id, VehicleRequestStatus from, VehicleRequestStatus to) {
        System.out.println("notify: request " + id + " " + from + " -> " + to);
    }
}
