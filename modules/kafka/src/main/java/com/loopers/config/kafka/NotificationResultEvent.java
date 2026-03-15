package com.loopers.config.kafka;

import java.util.List;

public record NotificationResultEvent(
    Long notificationId,
    boolean success,
    List<String> invalidTokens
) {}
