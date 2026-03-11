package com.loopers.application.appointment;

import com.loopers.application.appointment.AppointmentInfo;
import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.user.User;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.infrastructure.appointment.AppointmentJpaRepository;
import com.loopers.infrastructure.appointment.AppointmentParticipantJpaRepository;
import com.loopers.infrastructure.departureplace.DeparturePlaceJpaRepository;
import com.loopers.infrastructure.kakao.KakaoMobilityApiClient;
import com.loopers.infrastructure.odsay.OdsayApiClient;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
class AppointmentFacadeTravelTimeTest {

    @Autowired
    private AppointmentFacade appointmentFacade;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DeparturePlaceJpaRepository departurePlaceJpaRepository;

    @Autowired
    private AppointmentJpaRepository appointmentJpaRepository;

    @Autowired
    private AppointmentParticipantJpaRepository appointmentParticipantJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @MockitoBean
    private KakaoMobilityApiClient kakaoMobilityApiClient;

    @MockitoBean
    private OdsayApiClient odsayApiClient;

    private User host;
    private DeparturePlace departurePlace;
    private final ZonedDateTime futureDateTime = ZonedDateTime.now().plusDays(3);

    @BeforeEach
    void setUp() {
        host = userJpaRepository.save(User.create(11111L, "호스트", "https://example.com/host.jpg"));
        departurePlace = departurePlaceJpaRepository.save(
            DeparturePlace.create(host, "집", "서울 강남구 테헤란로 1", 37.4979, 127.0276));
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("약속 생성 시 이동시간 자동 계산 테스트")
    @Nested
    class Create {

        @DisplayName("출발지를 지정하면, 이동시간이 계산되어 반환된다.")
        @Test
        void returnsTravelTime_whenDeparturePlaceIsSet() {
            // arrange
            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(30);

            // act
            AppointmentInfo result = appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, departurePlace.getId());

            // assert
            assertThat(result.durationMinutes()).isNotNull();
            assertThat(result.departureAlertAt()).isNotNull();
        }

        @DisplayName("출발지가 없으면, 이동시간은 null이다.")
        @Test
        void returnNullTravelTime_whenNoDeparturePlace() {
            // act
            AppointmentInfo result = appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, null);

            // assert
            assertThat(result.durationMinutes()).isNull();
            assertThat(result.departureAlertAt()).isNull();
        }

        @DisplayName("이동시간 계산 API 장애 시, 예외 없이 null로 반환된다.")
        @Test
        void returnsNullTravelTime_whenApiThrowsException() {
            // arrange
            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willThrow(new RuntimeException("ODsay API 장애"));

            // act & assert - 예외가 전파되지 않아야 함
            AppointmentInfo result = appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, departurePlace.getId());

            assertThat(result.durationMinutes()).isNull();
            assertThat(result.departureAlertAt()).isNull();
        }
    }

    @DisplayName("약속 수정 시 선별 재계산 테스트")
    @Nested
    class UpdateAppointment {

        @DisplayName("좌표가 변경되면, 이동시간이 재계산된다.")
        @Test
        void recalculatesTravelTime_whenCoordinatesChange() {
            // arrange
            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(20);
            appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, departurePlace.getId());

            Long appointmentId = appointmentJpaRepository.findAll().get(0).getId();

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(45);

            // act
            AppointmentInfo result = appointmentFacade.updateAppointment(
                appointmentId, host.getId(), null, null, null,
                37.5665, 126.9780, null);

            // assert
            assertThat(result.durationMinutes()).isEqualTo(45);
        }

        @DisplayName("이름만 변경되면, 이동시간을 재계산하지 않는다.")
        @Test
        void doesNotRecalculate_whenOnlyNameChanges() {
            // arrange
            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(20);
            appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, departurePlace.getId());

            Long appointmentId = appointmentJpaRepository.findAll().get(0).getId();

            // act
            appointmentFacade.updateAppointment(
                appointmentId, host.getId(), "새 모임", null, null,
                null, null, null);

            // assert: create 시 1번, update 시에는 호출 없음 → 총 1번
            verify(odsayApiClient, org.mockito.Mockito.times(1))
                .calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }

        @DisplayName("약속 수정 중 이동시간 계산 API 장애 시, 예외 없이 완료된다.")
        @Test
        void completesWithoutException_whenApiThrowsDuringUpdate() {
            // arrange
            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(20);
            appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, departurePlace.getId());

            Long appointmentId = appointmentJpaRepository.findAll().get(0).getId();

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willThrow(new RuntimeException("API 장애"));

            // act & assert - 예외가 전파되지 않아야 함
            AppointmentInfo result = appointmentFacade.updateAppointment(
                appointmentId, host.getId(), null, null, null,
                37.5665, 126.9780, null);

            assertThat(result).isNotNull();
        }
    }

    @DisplayName("출발지 수정 시 장애 내성 테스트")
    @Nested
    class UpdateDeparture {

        @DisplayName("출발지 수정 시 이동시간 계산 API 장애가 발생해도, 예외 없이 완료된다.")
        @Test
        void completesWithoutException_whenApiThrowsDuringDepartureUpdate() {
            // arrange
            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(20);
            appointmentFacade.create(
                host.getId(), "모임", "강남역", "서울 강남구 강남대로 396",
                37.5000, 127.0300, futureDateTime,
                List.of(), TransportType.TRANSIT, null);

            Long appointmentId = appointmentJpaRepository.findAll().get(0).getId();

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willThrow(new RuntimeException("API 장애"));

            // act & assert - 예외가 전파되지 않아야 함
            DepartureUpdateInfo result = appointmentFacade.updateDeparture(
                appointmentId, host.getId(), departurePlace.getId(), TransportType.TRANSIT);

            assertThat(result).isNotNull();
            assertThat(result.durationMinutes()).isNull();
            assertThat(result.departureAlertAt()).isNull();
        }
    }
}
