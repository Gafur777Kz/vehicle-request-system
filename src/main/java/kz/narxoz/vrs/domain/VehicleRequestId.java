package kz.narxoz.vrs.domain;

import java.util.UUID;

/**
 * Суррогатный ключ заявки. В базе это колонка {@code id uuid PRIMARY KEY}.
 */
public record VehicleRequestId(String value) {

    public VehicleRequestId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор заявки не может быть пустым");
        }
        value = value.trim();
    }

    public static VehicleRequestId newId() {
        return new VehicleRequestId(UUID.randomUUID().toString());
    }

    /** UUID для колонки id; бросает IllegalArgumentException, если value не UUID. */
    public UUID asUuid() {
        return UUID.fromString(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
