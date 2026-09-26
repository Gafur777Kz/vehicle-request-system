package kz.narxoz.vrs.domain;

public class TransitionRule implements Rule {

    private final VehicleRequestPolicy policy;

    public TransitionRule(VehicleRequestPolicy policy) {
        this.policy = policy;
    }

    @Override
    public void check(VehicleRequestStatus from, VehicleRequestStatus to) {
        policy.move(from, to);
    }
}
