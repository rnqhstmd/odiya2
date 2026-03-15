package com.loopers.domain.notification;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    Optional<Notification> findById(Long id);
    Optional<Notification> findByEventId(String eventId);
    List<Notification> findByReceiverIdWithCursor(Long receiverId, Long cursor, int size);
    int countUnreadByReceiverId(Long receiverId);
    int markAllAsReadByReceiverId(Long receiverId);
    Notification save(Notification notification);

    boolean existsByReceiverIdAndTypeAndReferenceIdAndStatus(Long receiverId, NotificationType type, Long referenceId, NotificationStatus status);
    int cancelPendingByAppointmentId(Long appointmentId);
}
