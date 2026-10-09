package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.NotificationPort;
import kz.narxoz.vrs.domain.Rule;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestNumber;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import kz.narxoz.vrs.persistence.VehicleRequestJdbc;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleRequestService {

    private final Rule rules;
    private final NotificationPort notifications;
    private final VehicleRequestJdbc requests;

    public VehicleRequestService(Rule rules, NotificationPort notifications, VehicleRequestJdbc requests) {
        this.rules = rules;
        this.notifications = notifications;
        this.requests = requests;
    }

    /** Lab 2 + week 4: проверить переход и сообщить через исходящий порт. */
    public VehicleRequestStatus move(VehicleRequestId id, VehicleRequestStatus from, VehicleRequestStatus to) {
        rules.check(from, to);
        notifications.statusChanged(id, from, to);
        return to;
    }

    /**
     * Одна транзакция на вызов. Первый вызов коммитится.
     * Второй вызов с тем же номером бросает DuplicateVehicleRequest, первая строка остаётся.
     */
    @Transactional
    public void register(VehicleRequestId id, String number, String purpose) {
        requests.insert(id, new VehicleRequestNumber(number), VehicleRequestStatus.DRAFT, purpose);
    }

    /**
     * Одна транзакция, два INSERT с одним номером. Второй падает,
     * DuplicateVehicleRequest (unchecked) выходит из метода — Spring откатывает и первый INSERT.
     */
    @Transactional
    public void insertTwice(String number) {
        VehicleRequestNumber key = new VehicleRequestNumber(number);
        requests.insert(VehicleRequestId.newId(), key, VehicleRequestStatus.DRAFT, "Поездка в аэропорт");
        requests.insert(VehicleRequestId.newId(), key, VehicleRequestStatus.DRAFT, "Поездка в аэропорт ещё раз");
    }
}
