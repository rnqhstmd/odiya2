package com.loopers.config.kafka;

public final class NotificationTopics {
    public static final String SEND = "notification.send";
    public static final String RESULT = "notification.result";
    public static final String DLQ = "notification.send.dlq";
    private NotificationTopics() {}
}
