package com.loopers.config.kafka;

import java.util.List;

public record NotificationEvent(
    String eventId,
    Long notificationId,
    Long receiverId,
    List<String> tokens,
    String type,
    String title,
    String body,
    Long referenceId,
    String referenceType
) {}
