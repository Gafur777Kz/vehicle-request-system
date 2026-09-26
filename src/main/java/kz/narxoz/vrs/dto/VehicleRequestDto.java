package kz.narxoz.vrs.dto;

import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestStatus;

public record VehicleRequestDto(String id, String status) {

    public VehicleRequestId toId() {
        return new VehicleRequestId(id);
    }

    public VehicleRequestStatus toStatus() {
        return VehicleRequestStatus.valueOf(status.trim().toUpperCase());
    }
}
