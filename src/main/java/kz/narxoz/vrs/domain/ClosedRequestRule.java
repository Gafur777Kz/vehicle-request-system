package kz.narxoz.vrs.domain;

// стоп-фактор: закрытую заявку нельзя двигать, надо создать новую
public class ClosedRequestRule implements Rule {

    @Override
    public void check(VehicleRequestStatus from, VehicleRequestStatus to) {
        if (from == VehicleRequestStatus.COMPLETED || from == VehicleRequestStatus.REJECTED) {
            throw new IllegalStateException("Заявка закрыта (" + from + "), создайте новую");
        }
    }
}
