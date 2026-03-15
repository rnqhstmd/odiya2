package com.loopers.infrastructure.notification;

import com.loopers.domain.notification.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

    Optional<Notification> findByEventId(String eventId);

    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId AND (:cursor IS NULL OR n.id < :cursor) ORDER BY n.id DESC")
    List<Notification> findByReceiverIdWithCursor(@Param("receiverId") Long receiverId,
                                                   @Param("cursor") Long cursor,
                                                   Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver.id = :receiverId AND n.isRead = false")
    int countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.id = :receiverId AND n.isRead = false")
    int markAllAsReadByReceiverId(@Param("receiverId") Long receiverId);
}
