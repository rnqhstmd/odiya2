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

class AppointmentTest {

    private final User host = User.create(1L, "호스트", "https://example.com/host.jpg");
    private final ZonedDateTime futureDateTime = ZonedDateTime.now().plusDays(7);

    @DisplayName("약속을 생성할 때,")
    @Nested
    class Create {

        @DisplayName("정상적인 정보가 주어지면, 정상적으로 생성된다.")
        @Test
        void createsAppointment_whenValidInfoIsProvided() {
            // act
            Appointment appointment = Appointment.create(
                host, "저녁 모임", "강남역", "서울 강남구 강남대로 396",
                37.4979, 127.0276, futureDateTime);

            // assert
            assertAll(
                () -> assertThat(appointment.getHost()).isEqualTo(host),
                () -> assertThat(appointment.getName()).isEqualTo("저녁 모임"),
                () -> assertThat(appointment.getPlaceName()).isEqualTo("강남역"),
                () -> assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING)
            );
        }

        @DisplayName("이름이 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> Appointment.create(host, null, "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> Appointment.create(host, "   ", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름이 50자 초과이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNameExceeds50Characters() {
            // arrange
            String longName = "가".repeat(51);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> Appointment.create(host, longName, "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("위도가 범위를 벗어나면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenLatitudeOutOfRange() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> Appointment.create(host, "모임", "강남역", "서울 강남구", 91.0, 127.0276, futureDateTime));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("경도가 범위를 벗어나면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenLongitudeOutOfRange() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> Appointment.create(host, "모임", "강남역", "서울 강남구", 37.4979, 181.0, futureDateTime));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이름 앞뒤 공백이 trim 처리된다.")
        @Test
        void trimsName_whenNameHasWhitespace() {
            // act
            Appointment appointment = Appointment.create(
                host, " 저녁 모임 ", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // assert
            assertThat(appointment.getName()).isEqualTo("저녁 모임");
        }
    }

    @DisplayName("약속을 수정할 때,")
    @Nested
    class Update {

        @DisplayName("이름만 변경하면, 이름만 수정된다.")
        @Test
        void updatesOnlyName_whenOnlyNameIsProvided() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "기존 모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // act
            appointment.update("새 모임", null, null, null, null, null);

            // assert
            assertAll(
                () -> assertThat(appointment.getName()).isEqualTo("새 모임"),
                () -> assertThat(appointment.getPlaceName()).isEqualTo("강남역")
            );
        }

        @DisplayName("모든 필드가 null이면, 아무것도 변경되지 않는다.")
        @Test
        void changesNothing_whenAllFieldsAreNull() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "기존 모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // act
            appointment.update(null, null, null, null, null, null);

            // assert
            assertThat(appointment.getName()).isEqualTo("기존 모임");
        }
    }

    @DisplayName("약속을 취소할 때,")
    @Nested
    class Cancel {

        @DisplayName("상태가 CANCELLED로 변경되고 soft delete 된다.")
        @Test
        void cancelsAndSoftDeletes() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // act
            appointment.cancel();

            // assert
            assertAll(
                () -> assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED),
                () -> assertThat(appointment.getDeletedAt()).isNotNull()
            );
        }

        @DisplayName("이미 취소된 약속이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenAlreadyCancelled() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);
            appointment.cancel();

            // act
            CoreException result = assertThrows(CoreException.class, appointment::cancel);

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("전원 수락 시 CONFIRMED 전이를 확인할 때,")
    @Nested
    class ConfirmIfAllAccepted {

        @DisplayName("모든 비거절 참여자가 수락하면, CONFIRMED로 전이된다.")
        @Test
        void confirmsAppointment_whenAllNonRejectedAccepted() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);
            User friend = User.create(2L, "친구", "https://example.com/friend.jpg");

            AppointmentParticipant hostParticipant = AppointmentParticipant.create(
                appointment, host, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);
            appointment.addParticipant(hostParticipant);

            AppointmentParticipant friendParticipant = AppointmentParticipant.create(
                appointment, friend, ParticipantStatus.PENDING, TransportType.TRANSIT, null);
            appointment.addParticipant(friendParticipant);

            // act
            friendParticipant.accept();
            appointment.confirmIfAllAccepted();

            // assert
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }

        @DisplayName("PENDING 참여자가 남아있으면, PENDING 상태를 유지한다.")
        @Test
        void remainsPending_whenPendingParticipantExists() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);
            User friend1 = User.create(2L, "친구1", "https://example.com/f1.jpg");
            User friend2 = User.create(3L, "친구2", "https://example.com/f2.jpg");

            appointment.addParticipant(AppointmentParticipant.create(
                appointment, host, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null));
            AppointmentParticipant p1 = AppointmentParticipant.create(
                appointment, friend1, ParticipantStatus.PENDING, TransportType.TRANSIT, null);
            appointment.addParticipant(p1);
            appointment.addParticipant(AppointmentParticipant.create(
                appointment, friend2, ParticipantStatus.PENDING, TransportType.TRANSIT, null));

            // act
            p1.accept();
            appointment.confirmIfAllAccepted();

            // assert
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @DisplayName("거절된 참여자는 전이 조건에서 제외된다.")
        @Test
        void excludesRejectedParticipants() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);
            User friend1 = User.create(2L, "친구1", "https://example.com/f1.jpg");
            User friend2 = User.create(3L, "친구2", "https://example.com/f2.jpg");

            appointment.addParticipant(AppointmentParticipant.create(
                appointment, host, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null));
            AppointmentParticipant p1 = AppointmentParticipant.create(
                appointment, friend1, ParticipantStatus.PENDING, TransportType.TRANSIT, null);
            appointment.addParticipant(p1);
            AppointmentParticipant p2 = AppointmentParticipant.create(
                appointment, friend2, ParticipantStatus.PENDING, TransportType.TRANSIT, null);
            appointment.addParticipant(p2);

            // act
            p1.accept();
            p2.reject();
            appointment.confirmIfAllAccepted();

            // assert
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
        }
    }

    @DisplayName("CONFIRMED 상태에서 추가 초대 시,")
    @Nested
    class RevertToPendingIfConfirmed {

        @DisplayName("CONFIRMED이면, PENDING으로 롤백된다.")
        @Test
        void revertsToPending_whenConfirmed() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);
            AppointmentParticipant hostP = AppointmentParticipant.create(
                appointment, host, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);
            appointment.addParticipant(hostP);
            appointment.confirmIfAllAccepted();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);

            // act
            appointment.revertToPendingIfConfirmed();

            // assert
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }

        @DisplayName("PENDING이면, 그대로 유지된다.")
        @Test
        void remainsPending_whenAlreadyPending() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // act
            appointment.revertToPendingIfConfirmed();

            // assert
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PENDING);
        }
    }

    @DisplayName("참여자를 찾을 때,")
    @Nested
    class FindParticipant {

        @DisplayName("존재하는 사용자이면, 참여자를 반환한다.")
        @Test
        void returnsParticipant_whenUserExists() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);
            AppointmentParticipant hostP = AppointmentParticipant.create(
                appointment, host, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);
            appointment.addParticipant(hostP);

            // act
            AppointmentParticipant result = appointment.findParticipant(host.getId());

            // assert
            assertThat(result.getUser()).isEqualTo(host);
        }

        @DisplayName("존재하지 않는 사용자이면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenUserDoesNotExist() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> appointment.findParticipant(999L));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("호스트 여부를 확인할 때,")
    @Nested
    class IsHostUser {

        @DisplayName("호스트이면, true를 반환한다.")
        @Test
        void returnsTrue_whenUserIsHost() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // assert
            assertThat(appointment.isHostUser(host.getId())).isTrue();
        }

        @DisplayName("호스트가 아니면, false를 반환한다.")
        @Test
        void returnsFalse_whenUserIsNotHost() {
            // arrange
            Appointment appointment = Appointment.create(
                host, "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime);

            // assert
            assertThat(appointment.isHostUser(999L)).isFalse();
        }
    }
}
