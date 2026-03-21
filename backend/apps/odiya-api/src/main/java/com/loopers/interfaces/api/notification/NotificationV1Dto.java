package com.loopers.interfaces.api.notification;

import com.loopers.application.notification.NotificationInfo;
import com.loopers.application.notification.NotificationListInfo;

import java.time.ZonedDateTime;
import java.util.List;

public class NotificationV1Dto {

    public record NotificationResponse(
        Long id,
        String type,
        String title,
        String body,
        Long referenceId,
        String referenceType,
        boolean isRead,
        ZonedDateTime createdAt
    ) {
        public static NotificationResponse from(NotificationInfo info) {
            return new NotificationResponse(
                info.id(),
                info.type().name(),
                info.title(),
                info.body(),
                info.referenceId(),
                info.referenceType(),
                info.isRead(),
                info.createdAt()
            );
        }
    }

    public record NotificationListResponse(
        List<NotificationResponse> notifications,
        boolean hasNext,
        Long nextCursor
    ) {
        public static NotificationListResponse from(NotificationListInfo info) {
            List<NotificationResponse> responses = info.notifications().stream()
                .map(NotificationResponse::from)
                .toList();
            return new NotificationListResponse(responses, info.hasNext(), info.nextCursor());
        }
    }

    public record ReadAllResponse(
        int count
    ) {}

    public record UnreadCountResponse(
        int count
    ) {}
}
