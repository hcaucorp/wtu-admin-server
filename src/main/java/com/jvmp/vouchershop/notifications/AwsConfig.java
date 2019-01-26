package com.jvmp.vouchershop.notifications;


import com.jvmp.vouchershop.system.PropertyNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
public class AwsConfig {

    @Value(PropertyNames.AWS_SNS_ACCESS_KEY_ID)
    private String accessKeyId;
    @Value(PropertyNames.AWS_SNS_SECRET_KEY_ID)
    private String secretKeyId;

    @Bean
    public SnsClient snsClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretKeyId);
        return SnsClient.builder()
                .region(Region.EU_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
