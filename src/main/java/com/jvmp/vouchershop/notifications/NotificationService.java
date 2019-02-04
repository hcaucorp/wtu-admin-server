package com.jvmp.vouchershop.notifications;

public interface NotificationService {

    void pushOrderNotification(String message);

    void pushRedemptionNotification(String message);
}
