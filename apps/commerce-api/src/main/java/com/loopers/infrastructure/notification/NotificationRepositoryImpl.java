package com.loopers.infrastructure.notification;

import com.loopers.domain.notification.Notification;
import com.loopers.domain.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class NotificationRepositoryImpl implements NotificationRepository {

    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Optional<Notification> findById(Long id) {
        return notificationJpaRepository.findById(id);
    }

    @Override
    public Optional<Notification> findByEventId(String eventId) {
        return notificationJpaRepository.findByEventId(eventId);
    }

    @Override
    public List<Notification> findByReceiverIdWithCursor(Long receiverId, Long cursor, int size) {
        return notificationJpaRepository.findByReceiverIdWithCursor(receiverId, cursor, PageRequest.of(0, size));
    }

    @Override
    public int countUnreadByReceiverId(Long receiverId) {
        return notificationJpaRepository.countUnreadByReceiverId(receiverId);
    }

    @Override
    public int markAllAsReadByReceiverId(Long receiverId) {
        return notificationJpaRepository.markAllAsReadByReceiverId(receiverId);
    }

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }
}
