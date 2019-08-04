package es.coffeebyt.wtu.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {

    public static final String COUNTER_REDEMPTION_SUCCESS = "redemption.success";
    public static final String COUNTER_REDEMPTION_FAILURE = "redemption.failure";

    @Bean
    public Counter redemptionSuccessCounter(MeterRegistry registry) {
        return registry.counter(COUNTER_REDEMPTION_SUCCESS);
    }

    @Bean
    public Counter redemptionFailureCounter(MeterRegistry registry) {
        return registry.counter(COUNTER_REDEMPTION_FAILURE);
    }
}
