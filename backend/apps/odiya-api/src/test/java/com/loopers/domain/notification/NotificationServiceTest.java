package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private final User receiver = User.create(1L, "수신자", "https://example.com/receiver.jpg");
    private final User sender = User.create(2L, "발신자", "https://example.com/sender.jpg");

    private Notification buildNotification() {
        return Notification.create(
            "event-001", receiver, sender, NotificationType.APPOINTMENT_INVITE,
            "약속 초대", "모임에 초대되었습니다.", 10L, "APPOINTMENT");
    }

    @DisplayName("createIfAbsent를 호출할 때,")
    @Nested
    class CreateIfAbsent {

        @DisplayName("eventId에 해당하는 알림이 없으면 새로 생성하여 반환한다.")
        @Test
        void createsAndReturnsNewNotification_whenEventIdNotExists() {
            // arrange
            Notification notification = buildNotification();
            given(notificationRepository.findByEventId("event-001")).willReturn(Optional.empty());
            given(notificationRepository.save(any())).willReturn(notification);

            // act
            Notification result = notificationService.createIfAbsent(
                "event-001", receiver, sender, NotificationType.APPOINTMENT_INVITE,
                "약속 초대", "모임에 초대되었습니다.", 10L, "APPOINTMENT");

            // assert
            assertThat(result).isEqualTo(notification);
            verify(notificationRepository).save(any());
        }

        @DisplayName("eventId에 해당하는 알림이 이미 존재하면 기존 알림을 반환한다.")
        @Test
        void returnsExistingNotification_whenEventIdAlreadyExists() {
            // arrange
            Notification existing = buildNotification();
            given(notificationRepository.findByEventId("event-001")).willReturn(Optional.of(existing));

            // act
            Notification result = notificationService.createIfAbsent(
                "event-001", receiver, sender, NotificationType.APPOINTMENT_INVITE,
                "약속 초대", "모임에 초대되었습니다.", 10L, "APPOINTMENT");

            // assert
            assertThat(result).isEqualTo(existing);
            verify(notificationRepository, never()).save(any());
        }
    }

    @DisplayName("markAsRead를 호출할 때,")
    @Nested
    class MarkAsRead {

        @DisplayName("본인의 알림이면 읽음 처리된다.")
        @Test
        void marksAsRead_whenNotificationBelongsToUser() {
            // arrange
            Notification notification = buildNotification();
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));
            given(notificationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            notificationService.markAsRead(1L, receiver.getId());

            // assert
            assertThat(notification.isRead()).isTrue();
        }

        @DisplayName("타인의 알림이면 FORBIDDEN 예외가 발생한다.")
        @Test
        void throwsForbidden_whenNotificationBelongsToOtherUser() {
            // arrange
            Notification notification = buildNotification();
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> notificationService.markAsRead(1L, 999L));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.FORBIDDEN);
        }

        @DisplayName("존재하지 않는 알림이면 NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenNotificationDoesNotExist() {
            // arrange
            given(notificationRepository.findById(999L)).willReturn(Optional.empty());

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> notificationService.markAsRead(999L, receiver.getId()));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("countUnread를 호출할 때,")
    @Nested
    class CountUnread {

        @DisplayName("미읽음 수가 99를 초과하면 99를 반환한다.")
        @Test
        void returns99_whenUnreadCountExceeds99() {
            // arrange
            given(notificationRepository.countUnreadByReceiverId(receiver.getId())).willReturn(150);

            // act
            int result = notificationService.countUnread(receiver.getId());

            // assert
            assertThat(result).isEqualTo(99);
        }

        @DisplayName("미읽음 수가 99 이하이면 실제 수를 반환한다.")
        @Test
        void returnsActualCount_whenUnreadCountIs99OrLess() {
            // arrange
            given(notificationRepository.countUnreadByReceiverId(receiver.getId())).willReturn(50);

            // act
            int result = notificationService.countUnread(receiver.getId());

            // assert
            assertThat(result).isEqualTo(50);
        }
    }

    @DisplayName("cancelPendingByAppointmentId를 호출할 때,")
    @Nested
    class CancelPendingByAppointmentId {

        @DisplayName("repository의 cancelPendingByAppointmentId가 호출된다.")
        @Test
        void callsRepositoryCancelPending_withAppointmentId() {
            // arrange
            given(notificationRepository.cancelPendingByAppointmentId(10L)).willReturn(3);

            // act
            notificationService.cancelPendingByAppointmentId(10L);

            // assert
            verify(notificationRepository).cancelPendingByAppointmentId(10L);
        }
    }
}
