package kz.narxoz.vrs.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleRequestPolicyTest {

    private final VehicleRequestPolicy policy = new VehicleRequestPolicy();

    /** Две разрешённые строки из таблицы в README. */
    @ParameterizedTest
    @CsvSource({
            "DRAFT,     SUBMITTED",
            "SUBMITTED, APPROVED"
    })
    void allowedTransitionsReturnNewStatus(VehicleRequestStatus from, VehicleRequestStatus to) {
        assertEquals(to, policy.move(from, to));
    }

    /** Две запрещённые строки из таблицы в README. */
    @ParameterizedTest
    @CsvSource({
            "DRAFT,     APPROVED",
            "COMPLETED, ASSIGNED"
    })
    void forbiddenTransitionsThrow(VehicleRequestStatus from, VehicleRequestStatus to) {
        assertThrows(IllegalStateException.class, () -> policy.move(from, to));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void blankIdIsRejected(String value) {
        assertThrows(IllegalArgumentException.class, () -> new VehicleRequestId(value));
    }

    @Test
    void validIdIsKept() {
        assertEquals("VRS-1", new VehicleRequestId("VRS-1").value());
    }
}
