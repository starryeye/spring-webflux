package dev.practice.notification.port.out;

public interface NotificationStream {

    void pushMessage(String message);
}
