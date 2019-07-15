package es.coffeebyt.wtu.notifications;

public interface NotificationService {

    void pushOrderNotification(String message);

    void pushRedemptionNotification(String message);
}
