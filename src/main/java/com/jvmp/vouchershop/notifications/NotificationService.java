package com.jvmp.vouchershop.notifications;

public interface NotificationService {

    void push(String message, String topic);
}
