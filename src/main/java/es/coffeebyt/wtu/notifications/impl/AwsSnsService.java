package es.coffeebyt.wtu.notifications.impl;

import es.coffeebyt.wtu.notifications.NotificationService;
import es.coffeebyt.wtu.system.PropertyNames;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@RequiredArgsConstructor
@Component
@Profile("!local")
public class AwsSnsService implements NotificationService {

    @Value(PropertyNames.AWS_SNS_TOPIC_ORDERS)
    private String ordersTopic;

    @Value(PropertyNames.AWS_SNS_TOPIC_REDEMPTIONS)
    private String redemptionsTopic;

    private final SnsClient snsClient;

    @Override
    public void pushOrderNotification(String message) {
        push(message, "Order notification", ordersTopic);
    }

    @Override
    public void pushRedemptionNotification(String message) {
        push(message, "Redemption notification", redemptionsTopic);
    }

    private void push(String message, String subject, String topicArn) {
        PublishRequest publishRequest = PublishRequest.builder()
                .message(message)
                .subject(subject)
                .topicArn(topicArn)
                .build();

        snsClient.publish(publishRequest);
    }
}
