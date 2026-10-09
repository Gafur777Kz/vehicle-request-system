package kz.narxoz.vrs.persistence;

import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StatusColumnTest {

    @ParameterizedTest
    @EnumSource(VehicleRequestStatus.class)
    void everyStatusRoundTrips(VehicleRequestStatus status) {
        assertEquals(status, StatusColumn.fromText(StatusColumn.toText(status)));
    }

    @Test
    void unknownTextFromDatabaseThrows() {
        assertThrows(IllegalStateException.class, () -> StatusColumn.fromText("CANCELLED"));
        assertThrows(IllegalStateException.class, () -> StatusColumn.fromText("draft"));
    }
}
