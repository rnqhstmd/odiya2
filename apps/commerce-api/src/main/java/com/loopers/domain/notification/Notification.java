package com.loopers.domain.notification;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(name = "event_id", nullable = false, length = 64, unique = true)
    private String eventId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "body", nullable = false, length = 255)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    protected Notification() {}

    private Notification(String eventId, User receiver, User sender, NotificationType type,
                         String title, String body, Long referenceId, String referenceType) {
        this.eventId = eventId;
        this.receiver = receiver;
        this.sender = sender;
        this.type = type;
        this.title = title;
        this.body = body;
        this.status = NotificationStatus.PENDING;
        this.isRead = false;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
    }

    public static Notification create(String eventId, User receiver, User sender,
                                       NotificationType type, String title, String body,
                                       Long referenceId, String referenceType) {
        return new Notification(eventId, receiver, sender, type, title, body, referenceId, referenceType);
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public boolean isOwnedBy(Long userId) {
        return this.receiver.getId().equals(userId);
    }

}
