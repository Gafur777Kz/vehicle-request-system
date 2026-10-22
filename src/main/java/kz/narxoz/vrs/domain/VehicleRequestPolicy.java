package kz.narxoz.vrs.domain;

import java.util.Map;
import java.util.Set;

import static kz.narxoz.vrs.domain.VehicleRequestStatus.*;

public class VehicleRequestPolicy {

    private static final Map<VehicleRequestStatus, Set<VehicleRequestStatus>> ALLOWED = Map.of(
            DRAFT, Set.of(SUBMITTED),
            SUBMITTED, Set.of(APPROVED, REJECTED),
            APPROVED, Set.of(ASSIGNED),
            ASSIGNED, Set.of(COMPLETED),
            COMPLETED, Set.of(),
            REJECTED, Set.of()
    );

    public VehicleRequestStatus move(VehicleRequestStatus from, VehicleRequestStatus to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Статус не может быть null");
        }
        if (!ALLOWED.get(from).contains(to)) {
            throw new IllegalStateException("Переход " + from + " -> " + to + " запрещён");
        }
        return to;
    }
}
