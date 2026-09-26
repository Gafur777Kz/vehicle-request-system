package kz.narxoz.vrs.config;

import kz.narxoz.vrs.domain.ClosedRequestRule;
import kz.narxoz.vrs.domain.Rule;
import kz.narxoz.vrs.domain.RuleChain;
import kz.narxoz.vrs.domain.TransitionRule;
import kz.narxoz.vrs.domain.VehicleRequestPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class RuleConfig {

    @Bean
    public Rule rules() {
        return new RuleChain(List.of(
                new ClosedRequestRule(),
                new TransitionRule(new VehicleRequestPolicy())
        ));
    }
}
