package kz.narxoz.vrs.domain;

import java.util.List;

public class RuleChain implements Rule {

    private final List<Rule> rules;

    public RuleChain(List<Rule> rules) {
        this.rules = List.copyOf(rules);
    }

    @Override
    public void check(VehicleRequestStatus from, VehicleRequestStatus to) {
        for (Rule rule : rules) {
            rule.check(from, to);
        }
    }
}
