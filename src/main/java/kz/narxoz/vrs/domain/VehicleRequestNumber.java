package kz.narxoz.vrs.domain;

/**
 * Бизнес-ключ заявки — номер, который видит человек (VR-1042).
 * В базе это колонка {@code business_key text NOT NULL UNIQUE}.
 * Пустую строку база пропустит, поэтому отсекаем её здесь, как в VehicleRequestId.
 */
public record VehicleRequestNumber(String value) {

    public VehicleRequestNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Номер заявки не может быть пустым");
        }
        value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
