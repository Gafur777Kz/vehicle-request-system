package kz.narxoz.vrs.domain;

import java.util.UUID;

public record VehicleRequestId(String value) {

    public VehicleRequestId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор заявки не может быть пустым");
        }
    }

    public static VehicleRequestId newId() {
        return new VehicleRequestId(UUID.randomUUID().toString());
    }
}
