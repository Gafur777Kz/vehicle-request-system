package kz.narxoz.vrs.client;

import kz.narxoz.vrs.domain.NotificationPort;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.springframework.stereotype.Component;

// пока просто пишет в консоль, адрес соседа берётся из yaml
@Component
public class LogNotificationClient implements NotificationPort {

    private final DownstreamProperties downstream;

    public LogNotificationClient(DownstreamProperties downstream) {
        this.downstream = downstream;
    }

    @Override
    public void statusChanged(VehicleRequestId id, VehicleRequestStatus from, VehicleRequestStatus to) {
        System.out.println("notify " + downstream.baseUrl() + ": request " + id.value() + " " + from + " -> " + to);
    }
}
