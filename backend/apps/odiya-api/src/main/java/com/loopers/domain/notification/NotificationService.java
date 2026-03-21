package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private static final int MAX_UNREAD_COUNT_DISPLAY = 99;

    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification createIfAbsent(String eventId, User receiver, User sender,
                                        NotificationType type, String title, String body,
                                        Long referenceId, String referenceType) {
        Optional<Notification> existing = notificationRepository.findByEventId(eventId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Notification notification = Notification.create(eventId, receiver, sender, type, title, body,
            referenceId, referenceType);
        try {
            return notificationRepository.save(notification);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return notificationRepository.findByEventId(eventId)
                .orElseThrow(() -> new IllegalStateException("알림 생성 동시성 문제 해결 중 조회 실패: " + eventId, e));
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long receiverId, Long cursor, int size) {
        return notificationRepository.findByReceiverIdWithCursor(receiverId, cursor, size);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        if (!notification.isOwnedBy(userId)) {
            throw new CoreException(ErrorType.FORBIDDEN);
        }
        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationRepository.markAllAsReadByReceiverId(userId);
    }

    @Transactional(readOnly = true)
    public int countUnread(Long userId) {
        int count = notificationRepository.countUnreadByReceiverId(userId);
        return Math.min(count, MAX_UNREAD_COUNT_DISPLAY);
    }

    @Transactional
    public void cancelPendingByAppointmentId(Long appointmentId) {
        notificationRepository.cancelPendingByAppointmentId(appointmentId);
    }

    @Transactional
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
