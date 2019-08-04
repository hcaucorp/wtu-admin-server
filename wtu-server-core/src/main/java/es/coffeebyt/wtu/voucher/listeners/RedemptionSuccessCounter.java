package es.coffeebyt.wtu.voucher.listeners;

import static es.coffeebyt.wtu.metrics.ActuatorConfig.COUNTER_REDEMPTION_SUCCESS;

import org.springframework.stereotype.Component;

import es.coffeebyt.wtu.voucher.RedemptionListener;
import es.coffeebyt.wtu.voucher.impl.RedemptionRequest;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedemptionSuccessCounter implements RedemptionListener {

    private final MeterRegistry meterRegistry;

    @Override
    public void redeemed(RedemptionRequest redemptionRequest) {
        meterRegistry.counter(COUNTER_REDEMPTION_SUCCESS).increment();
    }
}
