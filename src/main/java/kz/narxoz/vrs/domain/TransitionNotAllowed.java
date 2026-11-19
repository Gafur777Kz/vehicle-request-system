package kz.narxoz.vrs.domain;

// запрещённый переход статуса, например DRAFT -> APPROVED
// наследник IllegalStateException, поэтому старые тесты Lab 1-2 не меняются
public class TransitionNotAllowed extends IllegalStateException {

    public TransitionNotAllowed(VehicleRequestStatus from, VehicleRequestStatus to) {
        super("cannot move from " + from + " to " + to);
    }
}
