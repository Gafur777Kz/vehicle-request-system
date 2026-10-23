package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.NotificationPort;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.persistence.VehicleRequestJpa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static kz.narxoz.vrs.domain.VehicleRequestStatus.APPROVED;
import static kz.narxoz.vrs.domain.VehicleRequestStatus.DRAFT;
import static kz.narxoz.vrs.domain.VehicleRequestStatus.SUBMITTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// без базы и без Spring, исходящий порт замокан
class VehicleRequestServiceTest {

    private NotificationPort notifications;
    private VehicleRequestService service;

    @BeforeEach
    void setUp() {
        notifications = mock(NotificationPort.class);
        service = new VehicleRequestService(new RuleConfig().rules(), notifications, mock(VehicleRequestJpa.class));
    }

    @Test
    void allowedMoveSendsNotification() {
        VehicleRequestId id = VehicleRequestId.newId();

        assertEquals(SUBMITTED, service.move(id, DRAFT, SUBMITTED));
        verify(notifications).statusChanged(id, DRAFT, SUBMITTED);
    }

    @Test
    void forbiddenMoveDoesNotSendNotification() {
        VehicleRequestId id = VehicleRequestId.newId();

        assertThrows(IllegalStateException.class, () -> service.move(id, DRAFT, APPROVED));
        verify(notifications, never()).statusChanged(any(), any(), any());
    }
}
