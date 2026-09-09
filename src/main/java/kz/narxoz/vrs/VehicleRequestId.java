package kz.narxoz.vrs;

/**
 * Идентификатор заявки на транспорт, например "VRS-1".
 * Пустой идентификатор запрещён: заявку без номера нельзя найти в системе.
 */
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
