package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createIfAbsent(String eventId, User receiver, User sender,
                                        NotificationType type, String title, String body,
                                        Long referenceId, String referenceType) {
        Optional<Notification> existing = notificationRepository.findByEventId(eventId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Notification notification = Notification.create(eventId, receiver, sender, type, title, body,
            referenceId, referenceType);
        return notificationRepository.save(notification);
    }

    public List<Notification> getNotifications(Long receiverId, Long cursor, int size) {
        return notificationRepository.findByReceiverIdWithCursor(receiverId, cursor, size);
    }

    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        if (!notification.isOwnedBy(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByReceiverId(userId);
    }

    public int countUnread(Long userId) {
        int count = notificationRepository.countUnreadByReceiverId(userId);
        return Math.min(count, 99);
    }

    public void updateStatus(Long notificationId, NotificationStatus status) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (status == NotificationStatus.SENT) {
                notification.markAsSent();
            } else if (status == NotificationStatus.FAILED) {
                notification.markAsFailed();
            }
            notificationRepository.save(notification);
        });
    }
}
