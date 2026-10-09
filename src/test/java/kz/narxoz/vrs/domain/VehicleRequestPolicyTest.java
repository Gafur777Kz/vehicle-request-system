package kz.narxoz.vrs.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Week 4: четыре строки из README. Разрешённые возвращают статус, запрещённые бросают. */
class VehicleRequestPolicyTest {

    private final VehicleRequestPolicy policy = new VehicleRequestPolicy();

    @ParameterizedTest(name = "{0} -> {1}: {2}")
    @CsvSource({
            "DRAFT,     SUBMITTED, allowed",
            "SUBMITTED, APPROVED,  allowed",
            "DRAFT,     APPROVED,  forbidden",
            "COMPLETED, DRAFT,     forbidden"
    })
    void readmeRows(VehicleRequestStatus from, VehicleRequestStatus to, String result) {
        if (result.equals("allowed")) {
            assertEquals(to, policy.move(from, to));
        } else {
            assertThrows(IllegalStateException.class, () -> policy.move(from, to));
        }
    }
}
