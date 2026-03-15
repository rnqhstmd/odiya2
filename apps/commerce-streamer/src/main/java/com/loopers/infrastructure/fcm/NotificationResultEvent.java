package com.loopers.infrastructure.fcm;

import java.util.List;

public record NotificationResultEvent(
    Long notificationId,
    boolean success,
    List<String> invalidTokens
) {}
