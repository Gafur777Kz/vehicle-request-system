package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.ClosedRequestRule;
import kz.narxoz.vrs.domain.Rule;
import kz.narxoz.vrs.domain.TransitionRule;
import kz.narxoz.vrs.domain.VehicleRequestPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RuleConfig {

    // сначала проверяем что заявка не закрыта, потом таблицу переходов
    @Bean
    public Rule rules() {
        Rule closed = new ClosedRequestRule();
        Rule transition = new TransitionRule(new VehicleRequestPolicy());
        return (from, to) -> {
            closed.check(from, to);
            transition.check(from, to);
        };
    }
}
