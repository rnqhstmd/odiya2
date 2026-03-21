package com.loopers.application.notification;

import com.loopers.config.kafka.NotificationEvent;
import com.loopers.config.kafka.NotificationEventPublisher;
import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentService;
import com.loopers.domain.notification.DeviceTokenService;
import com.loopers.domain.notification.Notification;
import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.notification.NotificationType;
import com.loopers.domain.nudge.NudgeCooldownRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationFacadeTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private DeviceTokenService deviceTokenService;

    @Mock
    private NotificationEventPublisher notificationEventPublisher;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private UserService userService;

    @Mock
    private NudgeCooldownRepository nudgeCooldownRepository;

    @InjectMocks
    private NotificationFacade notificationFacade;

    @DisplayName("sendNotification 테스트")
    @Nested
    class SendNotification {

        @DisplayName("정상 발송: DB 저장 및 Kafka 이벤트 발행이 호출된다.")
        @Test
        void sendNotification_정상발송() {
            // arrange
            User receiver = mock(User.class);
            User sender = mock(User.class);
            given(receiver.getId()).willReturn(1L);
            Notification notification = mock(Notification.class);
            given(notification.getId()).willReturn(10L);
            given(notificationService.createIfAbsent(
                anyString(), eq(receiver), eq(sender), any(), anyString(), anyString(), any(), any()))
                .willReturn(notification);
            given(deviceTokenService.getActiveTokenStrings(1L)).willReturn(List.of("token-abc"));

            // act
            notificationFacade.sendNotification(receiver, sender, NotificationType.NUDGE, "제목", "내용", 1L, "APPOINTMENT");

            // assert
            verify(notificationService).createIfAbsent(anyString(), eq(receiver), eq(sender),
                eq(NotificationType.NUDGE), eq("제목"), eq("내용"), eq(1L), eq("APPOINTMENT"));
            verify(notificationEventPublisher).publish(any(NotificationEvent.class));
        }

        @DisplayName("토큰 없음: Kafka 이벤트 발행이 호출되지 않는다.")
        @Test
        void sendNotification_토큰없음() {
            // arrange
            User receiver = mock(User.class);
            User sender = mock(User.class);
            given(receiver.getId()).willReturn(1L);
            Notification notification = mock(Notification.class);
            given(notificationService.createIfAbsent(
                anyString(), eq(receiver), eq(sender), any(), anyString(), anyString(), any(), any()))
                .willReturn(notification);
            given(deviceTokenService.getActiveTokenStrings(1L)).willReturn(List.of());

            // act
            notificationFacade.sendNotification(receiver, sender, NotificationType.NUDGE, "제목", "내용", null, null);

            // assert
            verify(notificationEventPublisher, never()).publish(any());
        }

        @DisplayName("예외 발생 시 로그만: 예외가 밖으로 전파되지 않는다.")
        @Test
        void sendNotification_예외발생시_로그만() {
            // arrange
            User receiver = mock(User.class);
            User sender = mock(User.class);
            given(receiver.getId()).willReturn(1L);
            given(notificationService.createIfAbsent(
                anyString(), eq(receiver), eq(sender), any(), anyString(), anyString(), any(), any()))
                .willThrow(new RuntimeException("DB 오류"));

            // act & assert - 예외가 전파되지 않아야 함
            assertDoesNotThrow(() ->
                notificationFacade.sendNotification(receiver, sender, NotificationType.NUDGE, "제목", "내용", null, null));
        }
    }

    @DisplayName("getNotifications 테스트")
    @Nested
    class GetNotifications {

        @DisplayName("정상 조회: size+1개 조회 후 hasNext가 true이면 size만큼 잘라서 반환한다.")
        @Test
        void getNotifications_정상조회() {
            // arrange
            int size = 3;
            List<Notification> notifications = new ArrayList<>();
            // 앞의 size개만 NotificationInfo.from()으로 매핑되므로, size개만 stub 필요
            for (int i = 0; i < size; i++) {
                Notification n = mock(Notification.class);
                given(n.getId()).willReturn((long) (i + 1));
                given(n.getType()).willReturn(NotificationType.NUDGE);
                given(n.getTitle()).willReturn("제목" + i);
                given(n.getBody()).willReturn("내용" + i);
                given(n.isRead()).willReturn(false);
                notifications.add(n);
            }
            // size+1번째 항목은 subList로 잘려서 from()이 호출되지 않으므로 stub 없이 추가
            notifications.add(mock(Notification.class));
            given(notificationService.getNotifications(1L, null, size + 1)).willReturn(notifications);

            // act
            NotificationListInfo result = notificationFacade.getNotifications(1L, null, size);

            // assert
            assertThat(result.hasNext()).isTrue();
            assertThat(result.notifications()).hasSize(size);
            assertThat(result.nextCursor()).isNotNull();
        }

        @DisplayName("최대 50 제한: size=100 요청 시 50으로 제한하여 조회한다.")
        @Test
        void getNotifications_최대50제한() {
            // arrange
            given(notificationService.getNotifications(eq(1L), any(), eq(51))).willReturn(List.of());

            // act
            NotificationListInfo result = notificationFacade.getNotifications(1L, null, 100);

            // assert
            verify(notificationService).getNotifications(1L, null, 51);
            assertThat(result.hasNext()).isFalse();
        }
    }

    @DisplayName("markAsRead 테스트")
    @Nested
    class MarkAsRead {

        @DisplayName("위임 확인: notificationService.markAsRead가 호출된다.")
        @Test
        void markAsRead_위임확인() {
            // act
            notificationFacade.markAsRead(10L, 1L);

            // assert
            verify(notificationService).markAsRead(10L, 1L);
        }
    }

    @DisplayName("getUnreadCount 테스트")
    @Nested
    class GetUnreadCount {

        @DisplayName("위임 확인: notificationService.countUnread가 호출된다.")
        @Test
        void getUnreadCount_위임확인() {
            // arrange
            given(notificationService.countUnread(1L)).willReturn(5);

            // act
            int result = notificationFacade.getUnreadCount(1L);

            // assert
            verify(notificationService).countUnread(1L);
            assertThat(result).isEqualTo(5);
        }
    }

    @DisplayName("nudge 테스트")
    @Nested
    class Nudge {

        @DisplayName("정상 발송: 약속 30분 전~약속 시간 전, 쿨다운 없을 때 발송된다.")
        @Test
        void nudge_정상발송() {
            // arrange
            long appointmentId = 1L;
            long senderUserId = 10L;
            long targetUserId = 20L;

            Appointment appointment = mock(Appointment.class);
            // 현재 시각 기준 15분 후가 약속 시간 → 30분 전 ~ 약속 시간 전 범위 안
            given(appointment.getDateTime()).willReturn(ZonedDateTime.now().plusMinutes(15));
            given(appointment.getName()).willReturn("저녁 약속");

            User sender = mock(User.class);
            given(sender.getNickname()).willReturn("홍길동");

            User target = mock(User.class);
            given(target.getId()).willReturn(targetUserId);

            AppointmentParticipant senderParticipant = mock(AppointmentParticipant.class);
            AppointmentParticipant targetParticipant = mock(AppointmentParticipant.class);

            given(appointmentService.getActiveAppointment(appointmentId)).willReturn(appointment);
            given(appointment.findParticipant(senderUserId)).willReturn(senderParticipant);
            given(appointment.findParticipant(targetUserId)).willReturn(targetParticipant);
            given(userService.getUser(senderUserId)).willReturn(sender);
            given(userService.getUser(targetUserId)).willReturn(target);
            given(nudgeCooldownRepository.trySetCooldown(appointmentId, senderUserId, targetUserId)).willReturn(true);

            // Notification service mock for sendNotification internal call
            Notification notification = mock(Notification.class);
            given(notificationService.createIfAbsent(anyString(), eq(target), eq(sender), any(), anyString(), anyString(), any(), any()))
                .willReturn(notification);
            given(deviceTokenService.getActiveTokenStrings(targetUserId)).willReturn(List.of());

            // act
            assertDoesNotThrow(() -> notificationFacade.nudge(appointmentId, senderUserId, List.of(targetUserId)));
        }

        @DisplayName("시간 범위 밖 - 30분 전 이전: BAD_REQUEST 예외가 발생한다.")
        @Test
        void nudge_시간범위밖_400() {
            // arrange
            long appointmentId = 1L;
            long senderUserId = 10L;

            Appointment appointment = mock(Appointment.class);
            // 약속 시간이 60분 후 → 30분 전보다 이전
            given(appointment.getDateTime()).willReturn(ZonedDateTime.now().plusMinutes(60));
            given(appointmentService.getActiveAppointment(appointmentId)).willReturn(appointment);

            // act
            CoreException ex = assertThrows(CoreException.class,
                () -> notificationFacade.nudge(appointmentId, senderUserId, List.of(20L)));

            // assert
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("쿨다운 - trySetCooldown false: TOO_MANY_REQUESTS 예외가 발생한다.")
        @Test
        void nudge_쿨다운_429() {
            // arrange
            long appointmentId = 1L;
            long senderUserId = 10L;
            long targetUserId = 20L;

            Appointment appointment = mock(Appointment.class);
            given(appointment.getDateTime()).willReturn(ZonedDateTime.now().plusMinutes(15));

            User sender = mock(User.class);

            AppointmentParticipant senderParticipant = mock(AppointmentParticipant.class);
            AppointmentParticipant targetParticipant = mock(AppointmentParticipant.class);

            given(appointmentService.getActiveAppointment(appointmentId)).willReturn(appointment);
            given(appointment.findParticipant(senderUserId)).willReturn(senderParticipant);
            given(appointment.findParticipant(targetUserId)).willReturn(targetParticipant);
            given(userService.getUser(senderUserId)).willReturn(sender);
            given(nudgeCooldownRepository.trySetCooldown(appointmentId, senderUserId, targetUserId)).willReturn(false);

            // act
            CoreException ex = assertThrows(CoreException.class,
                () -> notificationFacade.nudge(appointmentId, senderUserId, List.of(targetUserId)));

            // assert
            assertThat(ex.getErrorType()).isEqualTo(ErrorType.TOO_MANY_REQUESTS);
        }

        @DisplayName("자기 자신 제외: senderUserId가 targetUserIds에 포함되어도 자신에게 미발송한다.")
        @Test
        void nudge_자기자신제외() {
            // arrange
            long appointmentId = 1L;
            long senderUserId = 10L;

            Appointment appointment = mock(Appointment.class);
            given(appointment.getDateTime()).willReturn(ZonedDateTime.now().plusMinutes(15));

            User sender = mock(User.class);

            AppointmentParticipant senderParticipant = mock(AppointmentParticipant.class);

            given(appointmentService.getActiveAppointment(appointmentId)).willReturn(appointment);
            given(appointment.findParticipant(senderUserId)).willReturn(senderParticipant);
            given(userService.getUser(senderUserId)).willReturn(sender);

            // act - 자기 자신만 targetUserIds에 포함
            assertDoesNotThrow(() -> notificationFacade.nudge(appointmentId, senderUserId, List.of(senderUserId)));

            // assert - trySetCooldown은 호출되지 않아야 함 (자신은 필터링)
            verify(nudgeCooldownRepository, never()).trySetCooldown(anyLong(), anyLong(), anyLong());
        }
    }
}
