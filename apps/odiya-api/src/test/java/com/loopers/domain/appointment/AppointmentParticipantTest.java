package com.loopers.domain.appointment;

import com.loopers.domain.user.User;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentParticipantTest {

    private final User host = User.create(1L, "호스트", "https://example.com/host.jpg");
    private final User friend = User.create(2L, "친구", "https://example.com/friend.jpg");
    private final Appointment appointment = Appointment.create(
        host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, ZonedDateTime.now().plusDays(7));

    @DisplayName("참여자를 생성할 때,")
    @Nested
    class Create {

        @DisplayName("정상적인 정보가 주어지면, 정상적으로 생성된다.")
        @Test
        void createsParticipant_whenValidInfoIsProvided() {
            // act
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.PENDING, TransportType.TRANSIT, null);

            // assert
            assertAll(
                () -> assertThat(participant.getAppointment()).isEqualTo(appointment),
                () -> assertThat(participant.getUser()).isEqualTo(friend),
                () -> assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.PENDING),
                () -> assertThat(participant.getTransportType()).isEqualTo(TransportType.TRANSIT),
                () -> assertThat(participant.getDeparturePlace()).isNull()
            );
        }
    }

    @DisplayName("초대를 수락할 때,")
    @Nested
    class Accept {

        @DisplayName("PENDING 상태이면, ACCEPTED로 변경된다.")
        @Test
        void acceptsInvitation_whenPending() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.PENDING, TransportType.TRANSIT, null);

            // act
            participant.accept();

            // assert
            assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.ACCEPTED);
        }

        @DisplayName("이미 수락한 상태이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenAlreadyAccepted() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);

            // act
            CoreException result = assertThrows(CoreException.class, participant::accept);

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("거절한 상태이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenAlreadyRejected() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.REJECTED, TransportType.TRANSIT, null);

            // act
            CoreException result = assertThrows(CoreException.class, participant::accept);

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("초대를 거절할 때,")
    @Nested
    class Reject {

        @DisplayName("PENDING 상태이면, REJECTED로 변경된다.")
        @Test
        void rejectsInvitation_whenPending() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.PENDING, TransportType.TRANSIT, null);

            // act
            participant.reject();

            // assert
            assertThat(participant.getStatus()).isEqualTo(ParticipantStatus.REJECTED);
        }

        @DisplayName("이미 수락한 상태이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenAlreadyAccepted() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);

            // act
            CoreException result = assertThrows(CoreException.class, participant::reject);

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("출발 정보를 변경할 때,")
    @Nested
    class UpdateDeparture {

        @DisplayName("이동수단만 변경하면, 이동수단만 수정된다.")
        @Test
        void updatesOnlyTransportType_whenOnlyTransportTypeIsProvided() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.PENDING, TransportType.TRANSIT, null);

            // act
            participant.updateDeparture(null, TransportType.CAR_PARKING);

            // assert
            assertAll(
                () -> assertThat(participant.getTransportType()).isEqualTo(TransportType.CAR_PARKING),
                () -> assertThat(participant.getDeparturePlace()).isNull()
            );
        }

        @DisplayName("모두 null이면, 아무것도 변경되지 않는다.")
        @Test
        void changesNothing_whenAllNull() {
            // arrange
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.PENDING, TransportType.TRANSIT, null);

            // act
            participant.updateDeparture(null, null);

            // assert
            assertThat(participant.getTransportType()).isEqualTo(TransportType.TRANSIT);
        }
    }
}
