package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.Rule;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.springframework.stereotype.Service;

@Service
public class VehicleRequestService {

    private final Rule rules;

    public VehicleRequestService(Rule rules) {
        this.rules = rules;
    }

    public VehicleRequestStatus move(VehicleRequestStatus from, VehicleRequestStatus to) {
        rules.check(from, to);
        return to;
    }
}
