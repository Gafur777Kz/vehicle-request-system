package kz.narxoz.vrs.dto;

import kz.narxoz.vrs.domain.VehicleRequestStatus;

// JSON понадобится позже, пока просто перевод строки в статус
public record VehicleRequestDto(String id, String status) {

    public VehicleRequestStatus toStatus() {
        return VehicleRequestStatus.valueOf(status.trim().toUpperCase());
    }
}
