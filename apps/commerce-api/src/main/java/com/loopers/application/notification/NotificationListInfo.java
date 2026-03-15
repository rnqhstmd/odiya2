package com.loopers.application.notification;

import java.util.List;

public record NotificationListInfo(
    List<NotificationInfo> notifications,
    boolean hasNext,
    Long nextCursor
) {}
