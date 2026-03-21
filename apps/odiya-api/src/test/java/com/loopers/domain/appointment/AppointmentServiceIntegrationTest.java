package com.loopers.domain.appointment;

import com.loopers.domain.user.User;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.infrastructure.appointment.AppointmentJpaRepository;
import com.loopers.infrastructure.appointment.AppointmentParticipantJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class AppointmentServiceIntegrationTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentJpaRepository appointmentJpaRepository;

    @Autowired
    private AppointmentParticipantJpaRepository appointmentParticipantJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User host;
    private User friend;
    private final ZonedDateTime futureDateTime = ZonedDateTime.now().plusDays(7);

    @BeforeEach
    void setUp() {
        host = userJpaRepository.save(User.create(11111L, "호스트", "https://example.com/host.jpg"));
        friend = userJpaRepository.save(User.create(22222L, "친구", "https://example.com/friend.jpg"));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("약속을 생성할 때,")
    @Nested
    class Create {

        @DisplayName("정상적인 정보가 주어지면, 약속과 호스트 참여자가 생성된다.")
        @Test
        void createsAppointmentWithHostParticipant() {
            // act
            Appointment appointment = appointmentService.create(
                host, "저녁 모임", "강남역", "서울 강남구 강남대로 396",
                37.4979, 127.0276, futureDateTime, TransportType.TRANSIT, null);

            // assert
            assertAll(
                () -> assertThat(appointment.getId()).isNotNull(),
                () -> assertThat(appointment.getName()).isEqualTo("저녁 모임"),
                () -> assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING),
                () -> assertThat(appointment.getParticipants()).hasSize(1),
                () -> assertThat(appointment.getParticipants().get(0).getStatus())
                    .isEqualTo(ParticipantStatus.ACCEPTED)
            );
        }

        @DisplayName("과거 일시가 주어지면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenDateTimeIsInThePast() {
            // arrange
            ZonedDateTime pastDateTime = ZonedDateTime.now().minusDays(1);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> appointmentService.create(host, "모임", "강남역", "서울 강남구",
                    37.4979, 127.0276, pastDateTime, TransportType.TRANSIT, null));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("참여자를 추가할 때,")
    @Nested
    class AddParticipant {

        @DisplayName("정상적인 사용자가 주어지면, PENDING 상태로 추가된다.")
        @Test
        void addsParticipantAsPending() {
            // arrange
            Appointment appointment = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);

            // act
            AppointmentParticipant participant = appointmentService.addParticipant(
                appointment, friend, TransportType.TRANSIT);

            // assert
            assertAll(
                () -> assertThat(participant.getId()).isNotNull(),
                () -> assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.PENDING),
                () -> assertThat(participant.getTransportType()).isEqualTo(TransportType.TRANSIT)
            );
        }
    }

    @DisplayName("약속을 조회할 때,")
    @Nested
    class GetActiveAppointment {

        @DisplayName("존재하는 약속이면, 해당 약속을 반환한다.")
        @Test
        void returnsAppointment_whenExists() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);

            // act
            Appointment result = appointmentService.getActiveAppointment(saved.getId());

            // assert
            assertThat(result.getId()).isEqualTo(saved.getId());
        }

        @DisplayName("존재하지 않는 ID이면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenNotExists() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> appointmentService.getActiveAppointment(999L));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("취소된 약속이면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenCancelled() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);
            appointmentService.cancel(saved.getId(), host.getId());

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> appointmentService.getActiveAppointment(saved.getId()));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("약속을 수정할 때,")
    @Nested
    class Update {

        @DisplayName("호스트가 이름을 변경하면, 이름이 수정된다.")
        @Test
        void updatesName_whenHostChangesName() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "기존 모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);

            // act
            Appointment result = appointmentService.update(
                saved.getId(), host.getId(), "새 모임", null, null, null, null, null);

            // assert
            assertThat(result.getName()).isEqualTo("새 모임");
        }

        @DisplayName("호스트가 아닌 사용자이면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorized_whenNotHost() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> appointmentService.update(saved.getId(), friend.getId(),
                    "새 모임", null, null, null, null, null));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    @DisplayName("약속을 취소할 때,")
    @Nested
    class Cancel {

        @DisplayName("호스트가 취소하면, soft delete 된다.")
        @Test
        void cancelsAppointment_whenHostCancels() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);

            // act
            appointmentService.cancel(saved.getId(), host.getId());

            // assert
            CoreException result = assertThrows(CoreException.class,
                () -> appointmentService.getActiveAppointment(saved.getId()));
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("초대를 수락할 때,")
    @Nested
    class AcceptInvitation {

        @DisplayName("PENDING 참여자가 수락하면, ACCEPTED로 변경된다.")
        @Test
        void acceptsInvitation_whenPending() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);
            appointmentService.addParticipant(saved, friend, TransportType.TRANSIT);

            // act
            appointmentService.acceptInvitation(saved.getId(), friend.getId());

            // assert
            AppointmentParticipant participant = appointmentParticipantJpaRepository
                .findByAppointmentIdAndUserId(saved.getId(), friend.getId()).orElseThrow();
            assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.ACCEPTED);
        }

        @DisplayName("전원 수락 시 CONFIRMED로 전이된다.")
        @Test
        void confirmsAppointment_whenAllAccepted() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);
            appointmentService.addParticipant(saved, friend, TransportType.TRANSIT);

            // act
            appointmentService.acceptInvitation(saved.getId(), friend.getId());

            // assert
            Appointment result = appointmentService.getActiveAppointment(saved.getId());
            assertThat(result.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }
    }

    @DisplayName("초대를 거절할 때,")
    @Nested
    class RejectInvitation {

        @DisplayName("PENDING 참여자가 거절하면, REJECTED로 변경된다.")
        @Test
        void rejectsInvitation_whenPending() {
            // arrange
            Appointment saved = appointmentService.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                TransportType.TRANSIT, null);
            appointmentService.addParticipant(saved, friend, TransportType.TRANSIT);

            // act
            appointmentService.rejectInvitation(saved.getId(), friend.getId());

            // assert
            AppointmentParticipant participant = appointmentParticipantJpaRepository
                .findByAppointmentIdAndUserId(saved.getId(), friend.getId()).orElseThrow();
            assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.REJECTED);
        }
    }

    @DisplayName("UPCOMING 약속 목록을 조회할 때,")
    @Nested
    class GetUpcomingAppointments {

        @DisplayName("미래 약속이 있으면, 목록을 반환한다.")
        @Test
        void returnsUpcomingAppointments() {
            // arrange
            appointmentService.create(
                host, "모임1", "강남역", "서울 강남구", 37.4979, 127.0276,
                ZonedDateTime.now().plusDays(1), TransportType.TRANSIT, null);
            appointmentService.create(
                host, "모임2", "홍대역", "서울 마포구", 37.5563, 126.9237,
                ZonedDateTime.now().plusDays(2), TransportType.TRANSIT, null);

            // act
            List<Appointment> result = appointmentService.getUpcomingAppointments(
                host.getId(), null, 10);

            // assert
            assertThat(result).hasSize(2);
        }
    }
}
