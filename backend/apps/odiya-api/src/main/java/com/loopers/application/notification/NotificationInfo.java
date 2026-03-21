package com.loopers.application.notification;

import com.loopers.domain.notification.Notification;
import com.loopers.domain.notification.NotificationType;

import java.time.ZonedDateTime;

public record NotificationInfo(
    Long id,
    NotificationType type,
    String title,
    String body,
    Long referenceId,
    String referenceType,
    boolean isRead,
    ZonedDateTime createdAt
) {
    public static NotificationInfo from(Notification notification) {
        return new NotificationInfo(
            notification.getId(),
            notification.getType(),
            notification.getTitle(),
            notification.getBody(),
            notification.getReferenceId(),
            notification.getReferenceType(),
            notification.isRead(),
            notification.getCreatedAt()
        );
    }
}
