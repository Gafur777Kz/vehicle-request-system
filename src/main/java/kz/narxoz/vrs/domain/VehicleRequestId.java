package kz.narxoz.vrs.domain;


public record VehicleRequestId(String value) {

    public VehicleRequestId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Идентификатор заявки не может быть пустым");
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
