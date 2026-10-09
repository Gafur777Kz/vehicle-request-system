package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.ClosedRequestRule;
import kz.narxoz.vrs.domain.NotificationPort;
import kz.narxoz.vrs.domain.RuleChain;
import kz.narxoz.vrs.domain.TransitionRule;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestPolicy;
import kz.narxoz.vrs.persistence.VehicleRequestJdbc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static kz.narxoz.vrs.domain.VehicleRequestStatus.APPROVED;
import static kz.narxoz.vrs.domain.VehicleRequestStatus.DRAFT;
import static kz.narxoz.vrs.domain.VehicleRequestStatus.SUBMITTED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class VehicleRequestServiceTest {

    private NotificationPort notifications;
    private VehicleRequestService service;

    @BeforeEach
    void setUp() {
        notifications = mock(NotificationPort.class);
        VehicleRequestJdbc unusedHere = mock(VehicleRequestJdbc.class);
        service = new VehicleRequestService(
                new RuleChain(List.of(new ClosedRequestRule(), new TransitionRule(new VehicleRequestPolicy()))),
                notifications,
                unusedHere);
    }

    @Test
    void allowedMoveNotifiesOnce() {
        VehicleRequestId id = VehicleRequestId.newId();

        assertEquals(SUBMITTED, service.move(id, DRAFT, SUBMITTED));

        verify(notifications).statusChanged(id, DRAFT, SUBMITTED);
    }

    @Test
    void forbiddenMoveDoesNotNotify() {
        VehicleRequestId id = VehicleRequestId.newId();

        assertThrows(IllegalStateException.class, () -> service.move(id, DRAFT, APPROVED));

        verify(notifications, never()).statusChanged(any(), any(), any());
    }
}
