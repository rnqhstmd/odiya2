package com.loopers.application.notification;

import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentService;
import com.loopers.domain.notification.DeviceTokenService;
import com.loopers.domain.notification.NotificationEvent;
import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.notification.NotificationType;
import com.loopers.domain.notification.Notification;
import com.loopers.domain.nudge.NudgeCooldownRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.notification.NotificationEventPublisher;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationFacade {

    private final NotificationService notificationService;
    private final DeviceTokenService deviceTokenService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final NudgeCooldownRepository nudgeCooldownRepository;

    public void sendNotification(User receiver, User sender, NotificationType type,
                                  String title, String body, Long referenceId, String referenceType) {
        try {
            String eventId = UUID.randomUUID().toString();
            Notification notification = notificationService.createIfAbsent(
                eventId, receiver, sender, type, title, body, referenceId, referenceType);

            List<String> tokens = deviceTokenService.getActiveTokenStrings(receiver.getId());
            if (tokens.isEmpty()) {
                log.info("활성 디바이스 토큰 없음: userId={}, type={}", receiver.getId(), type);
                return;
            }

            NotificationEvent event = new NotificationEvent(
                eventId, notification.getId(), receiver.getId(), tokens,
                type.name(), title, body, referenceId, referenceType);
            notificationEventPublisher.publish(event);
        } catch (Exception e) {
            log.warn("알림 발송 실패: receiverId={}, type={}, error={}", receiver.getId(), type, e.getMessage(), e);
        }
    }

    @Transactional
    public void registerDevice(Long userId, String token, com.loopers.domain.notification.DeviceType deviceType) {
        User user = userService.getUser(userId);
        deviceTokenService.register(user, token, deviceType);
    }

    public void deactivateDevice(String token, Long userId) {
        deviceTokenService.deactivate(token, userId);
    }

    @Transactional(readOnly = true)
    public NotificationListInfo getNotifications(Long userId, Long cursor, int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        List<Notification> notifications = notificationService.getNotifications(userId, cursor, safeSize + 1);

        boolean hasNext = notifications.size() > safeSize;
        if (hasNext) {
            notifications = notifications.subList(0, safeSize);
        }

        List<NotificationInfo> infos = notifications.stream()
            .map(NotificationInfo::from)
            .toList();

        Long nextCursor = hasNext && !infos.isEmpty()
            ? infos.get(infos.size() - 1).id()
            : null;

        return new NotificationListInfo(infos, hasNext, nextCursor);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationService.markAsRead(notificationId, userId);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationService.markAllAsRead(userId);
    }

    @Transactional(readOnly = true)
    public int getUnreadCount(Long userId) {
        return notificationService.countUnread(userId);
    }

    @Transactional
    public void nudge(Long appointmentId, Long senderUserId, List<Long> targetUserIds) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime appointmentTime = appointment.getDateTime();
        if (now.isBefore(appointmentTime.minusMinutes(30)) || !now.isBefore(appointmentTime)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "콕 찌르기는 약속 30분 전부터 약속 시간 전까지만 가능합니다.");
        }

        appointment.findParticipant(senderUserId);
        User sender = userService.getUser(senderUserId);

        // 1단계: 쿨다운 사전 검증
        for (Long targetUserId : targetUserIds) {
            if (targetUserId.equals(senderUserId)) {
                continue;
            }
            appointment.findParticipant(targetUserId);

            if (nudgeCooldownRepository.existsCooldown(appointmentId, senderUserId, targetUserId)) {
                throw new CoreException(ErrorType.TOO_MANY_REQUESTS, "잠시 후 다시 시도해주세요.");
            }
        }

        // 2단계: 쿨다운 설정 + 알림 발송
        for (Long targetUserId : targetUserIds) {
            if (targetUserId.equals(senderUserId)) {
                continue;
            }
            nudgeCooldownRepository.setCooldown(appointmentId, senderUserId, targetUserId);

            User target = userService.getUser(targetUserId);
            String title = sender.getNickname() + "님이 콕 찔렀어요!";
            String body = appointment.getName() + " 약속에 서둘러주세요.";
            sendNotification(target, sender, NotificationType.NUDGE, title, body,
                appointmentId, "APPOINTMENT");
        }
    }
}
