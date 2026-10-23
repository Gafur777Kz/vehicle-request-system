package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.DuplicateVehicleRequest;
import kz.narxoz.vrs.domain.NotificationPort;
import kz.narxoz.vrs.domain.Rule;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import kz.narxoz.vrs.persistence.VehicleRequestEntity;
import kz.narxoz.vrs.persistence.VehicleRequestJpa;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VehicleRequestService {

    private final Rule rules;
    private final NotificationPort notifications;
    private final VehicleRequestJpa requests;

    public VehicleRequestService(Rule rules, NotificationPort notifications, VehicleRequestJpa requests) {
        this.rules = rules;
        this.notifications = notifications;
        this.requests = requests;
    }

    public VehicleRequestStatus move(VehicleRequestId id, VehicleRequestStatus from, VehicleRequestStatus to) {
        rules.check(from, to);
        notifications.statusChanged(id, from, to);
        return to;
    }

    // одна транзакция на вызов: второй register с тем же номером падает, первая заявка остаётся
    @Transactional
    public void register(VehicleRequestId id, String number, String purpose) {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Номер заявки не может быть пустым");
        }
        try {
            requests.insert(toEntity(id, number, VehicleRequestStatus.DRAFT, purpose));
            requests.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateVehicleRequest(number);
        }
    }

    // одна транзакция, два insert с одним номером -> второй падает, откатывается всё (count = 0)
    @Transactional
    public void insertTwice(String number) {
        requests.insert(toEntity(VehicleRequestId.newId(), number, VehicleRequestStatus.DRAFT, "Поездка в аэропорт"));
        try {
            requests.insert(toEntity(VehicleRequestId.newId(), number, VehicleRequestStatus.DRAFT, "Ещё раз"));
            requests.flush();
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateVehicleRequest(number);
        }
    }

    private VehicleRequestEntity toEntity(VehicleRequestId id, String number,
                                          VehicleRequestStatus status, String purpose) {
        VehicleRequestEntity row = new VehicleRequestEntity();
        row.setId(UUID.fromString(id.value()));
        row.setBusinessKey(number);
        row.setStatus(status.name());
        row.setPurpose(purpose);
        return row;
    }
}
