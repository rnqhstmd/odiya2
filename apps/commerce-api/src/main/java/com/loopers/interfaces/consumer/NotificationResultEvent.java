package com.loopers.interfaces.consumer;

import java.util.List;

public record NotificationResultEvent(
    Long notificationId,
    boolean success,
    List<String> invalidTokens
) {}
