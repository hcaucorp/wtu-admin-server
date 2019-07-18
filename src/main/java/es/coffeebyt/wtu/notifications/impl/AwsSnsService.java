package es.coffeebyt.wtu.notifications.impl;

import es.coffeebyt.wtu.notifications.NotificationService;
import es.coffeebyt.wtu.system.PropertyNames;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

import static es.coffeebyt.wtu.metrics.ActuatorConfig.COUNTER_REDEMPTION_FAILURE;
import static es.coffeebyt.wtu.metrics.ActuatorConfig.COUNTER_REDEMPTION_SUCCESS;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class AwsSnsService implements NotificationService {

    @Value(PropertyNames.AWS_SNS_TOPIC_REDEMPTIONS)
    private String redemptionsTopic;

    private final SnsClient snsClient;
    private final MeterRegistry meterRegistry;

    @Override
    public void pushNotification(String subject, String message) {
        push(message, subject, redemptionsTopic);
    }

    private void push(String message, String subject, String topicArn) {
        PublishRequest publishRequest = PublishRequest.builder()
                .message(message)
                .subject(subject)
                .topicArn(topicArn)
                .build();

        snsClient.publish(publishRequest);
    }

    @Scheduled(cron = "0 1 * * * ?")
    public void pushNotification() {
        Counter successCounter = meterRegistry.counter(COUNTER_REDEMPTION_SUCCESS);
        Counter failedCounter = meterRegistry.counter(COUNTER_REDEMPTION_FAILURE);

        int successful = (int) successCounter.count();
        int failed = (int) failedCounter.count();

        if (successful + failed == 0) {
            return;
        }

        String subject = "Daily redemption summary";
        String message = String.format("Successful redemptions: %d \nFailed redemptions:     %d", successful, failed);

        pushNotification(subject, message);
    }
}
