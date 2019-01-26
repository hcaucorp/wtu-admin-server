package com.jvmp.vouchershop.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@RequiredArgsConstructor
@Component
class AwsSnsService implements NotificationService {

    private final SnsClient snsClient;

    @Override
    public void push(String message, String topic) {

        //publish to an SNS topic
        PublishRequest publishRequest = PublishRequest.builder()
                .message(message)
                .topicArn(topic)
                .build();

        snsClient.publish(publishRequest);
    }
}
