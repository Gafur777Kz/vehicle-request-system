package kz.narxoz.vrs.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VehicleRequestIdTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void nullOrBlankIdThrows(String value) {
        assertThrows(IllegalArgumentException.class, () -> new VehicleRequestId(value));
    }

    @Test
    void newIdIsDifferentEveryTime() {
        assertNotEquals(VehicleRequestId.newId(), VehicleRequestId.newId());
    }
}
