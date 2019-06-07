package com.jvmp.vouchershop.notifications.impl;

import com.jvmp.vouchershop.notifications.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Use this implementation when running on localhost to prevent using AWS services and generate unnecessary costs.
 */
@Slf4j
@Component
@Profile("local")
public class NoOpNotificationService implements NotificationService {

    @Override
    public void pushOrderNotification(String message) { }

    @Override
    public void pushRedemptionNotification(String message) { }
}
