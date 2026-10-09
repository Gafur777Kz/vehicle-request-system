package kz.narxoz.vrs.persistence;

import kz.narxoz.vrs.domain.VehicleRequestStatus;

/**
 * Перевод статуса enum <-> текст в колонке status.
 * Неизвестный текст из базы — ошибка, а не новый статус.
 */
final class StatusColumn {

    private StatusColumn() {
    }

    static String toText(VehicleRequestStatus status) {
        return switch (status) {
            case DRAFT -> "DRAFT";
            case SUBMITTED -> "SUBMITTED";
            case APPROVED -> "APPROVED";
            case ASSIGNED -> "ASSIGNED";
            case COMPLETED -> "COMPLETED";
            case REJECTED -> "REJECTED";
        };
    }

    static VehicleRequestStatus fromText(String text) {
        if (text == null) {
            throw new IllegalStateException("status is null in database");
        }
        return switch (text) {
            case "DRAFT" -> VehicleRequestStatus.DRAFT;
            case "SUBMITTED" -> VehicleRequestStatus.SUBMITTED;
            case "APPROVED" -> VehicleRequestStatus.APPROVED;
            case "ASSIGNED" -> VehicleRequestStatus.ASSIGNED;
            case "COMPLETED" -> VehicleRequestStatus.COMPLETED;
            case "REJECTED" -> VehicleRequestStatus.REJECTED;
            default -> throw new IllegalStateException("unknown status in database: " + text);
        };
    }
}
