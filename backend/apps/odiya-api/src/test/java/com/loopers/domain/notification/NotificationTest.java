package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationTest {

    private final User receiver = User.create(1L, "수신자", "https://example.com/receiver.jpg");
    private final User sender = User.create(2L, "발신자", "https://example.com/sender.jpg");

    private Notification buildNotification() {
        return Notification.create(
            "event-001", receiver, sender, NotificationType.APPOINTMENT_INVITE,
            "약속 초대", "모임에 초대되었습니다.", 10L, "APPOINTMENT");
    }

    @DisplayName("알림을 생성할 때,")
    @Nested
    class Create {

        @DisplayName("정상적인 정보가 주어지면, 모든 필드가 설정되고 기본 상태는 PENDING, isRead=false이다.")
        @Test
        void createsNotification_withDefaultPendingStatusAndUnread() {
            // act
            Notification notification = buildNotification();

            // assert
            assertAll(
                () -> assertThat(notification.getEventId()).isEqualTo("event-001"),
                () -> assertThat(notification.getReceiver()).isEqualTo(receiver),
                () -> assertThat(notification.getSender()).isEqualTo(sender),
                () -> assertThat(notification.getType()).isEqualTo(NotificationType.APPOINTMENT_INVITE),
                () -> assertThat(notification.getTitle()).isEqualTo("약속 초대"),
                () -> assertThat(notification.getBody()).isEqualTo("모임에 초대되었습니다."),
                () -> assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING),
                () -> assertThat(notification.isRead()).isFalse()
            );
        }
    }

    @DisplayName("읽음 처리할 때,")
    @Nested
    class MarkAsRead {

        @DisplayName("markAsRead() 호출 후 isRead가 true가 된다.")
        @Test
        void setsIsReadToTrue_afterMarkAsRead() {
            // arrange
            Notification notification = buildNotification();

            // act
            notification.markAsRead();

            // assert
            assertThat(notification.isRead()).isTrue();
        }

        @DisplayName("2회 연속 호출해도 예외가 발생하지 않는다 (멱등성).")
        @Test
        void isIdempotent_whenCalledTwice() {
            // arrange
            Notification notification = buildNotification();
            notification.markAsRead();

            // act & assert
            assertDoesNotThrow(notification::markAsRead);
            assertThat(notification.isRead()).isTrue();
        }
    }

    @DisplayName("발송 처리할 때,")
    @Nested
    class MarkAsSent {

        @DisplayName("PENDING 상태에서 markAsSent() 호출 시 SENT로 전이된다.")
        @Test
        void transitionsToSent_fromPending() {
            // arrange
            Notification notification = buildNotification();

            // act
            notification.markAsSent();

            // assert
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        }
    }

    @DisplayName("실패 처리할 때,")
    @Nested
    class MarkAsFailed {

        @DisplayName("PENDING 상태에서 markAsFailed() 호출 시 FAILED로 전이된다.")
        @Test
        void transitionsToFailed_fromPending() {
            // arrange
            Notification notification = buildNotification();

            // act
            notification.markAsFailed();

            // assert
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
        }
    }

    @DisplayName("소유자를 확인할 때,")
    @Nested
    class IsOwnedBy {

        @DisplayName("수신자 ID와 일치하면 true를 반환한다.")
        @Test
        void returnsTrue_whenUserIsReceiver() {
            // arrange
            Notification notification = buildNotification();

            // assert
            assertThat(notification.isOwnedBy(receiver.getId())).isTrue();
        }

        @DisplayName("수신자 ID와 일치하지 않으면 false를 반환한다.")
        @Test
        void returnsFalse_whenUserIsNotReceiver() {
            // arrange
            Notification notification = buildNotification();

            // assert
            assertThat(notification.isOwnedBy(999L)).isFalse();
        }
    }
}
