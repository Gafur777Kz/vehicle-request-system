package kz.narxoz.vrs.domain;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class VehicleRequestPolicy {

    private static final Map<VehicleRequestStatus, Set<VehicleRequestStatus>> ALLOWED =
            new EnumMap<>(VehicleRequestStatus.class);

    static {
        ALLOWED.put(VehicleRequestStatus.DRAFT,
                Set.of(VehicleRequestStatus.SUBMITTED));
        ALLOWED.put(VehicleRequestStatus.SUBMITTED,
                Set.of(VehicleRequestStatus.APPROVED, VehicleRequestStatus.REJECTED));
        ALLOWED.put(VehicleRequestStatus.APPROVED,
                Set.of(VehicleRequestStatus.ASSIGNED));
        ALLOWED.put(VehicleRequestStatus.ASSIGNED,
                Set.of(VehicleRequestStatus.COMPLETED));
        ALLOWED.put(VehicleRequestStatus.COMPLETED, Set.of());
        ALLOWED.put(VehicleRequestStatus.REJECTED, Set.of());
    }

    /**
     * Переводит заявку из статуса from в статус to.
     *
     * @return новый статус, если переход разрешён
     * @throws IllegalStateException если переход запрещён бизнес-правилами
     */
    public VehicleRequestStatus move(VehicleRequestStatus from, VehicleRequestStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Статус не может быть null");
        }

        Set<VehicleRequestStatus> next = ALLOWED.get(from);
        if (!next.contains(to)) {
            throw new IllegalStateException("Переход " + from + " -> " + to + " запрещён");
        }

        return to;
    }
}
