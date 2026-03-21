package com.loopers.domain.traveltime;

import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.ParticipantStatus;
import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.user.User;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.domain.usersettings.UserSettings;
import com.loopers.domain.usersettings.UserSettingsRepository;
import com.loopers.infrastructure.kakao.KakaoMobilityApiClient;
import com.loopers.infrastructure.odsay.OdsayApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TravelTimeServiceTest {

    @Mock
    private TravelTimeRepository travelTimeRepository;

    @Mock
    private TravelTimeCacheRepository travelTimeCacheRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private KakaoMobilityApiClient kakaoMobilityApiClient;

    @Mock
    private OdsayApiClient odsayApiClient;

    @InjectMocks
    private TravelTimeService travelTimeService;

    private AppointmentParticipant makeParticipant(TransportType transportType,
                                                    double originLat, double originLng,
                                                    double destLat, double destLng) {
        User user = User.create(1L, "테스트유저", "https://example.com/img.jpg");
        DeparturePlace departurePlace = DeparturePlace.create(user, "집", "서울 강남구", originLat, originLng);
        Appointment appointment = Appointment.create(
            user, "약속", "목적지", "서울 종로구", destLat, destLng,
            ZonedDateTime.now().plusDays(1));
        return AppointmentParticipant.create(appointment, user, ParticipantStatus.ACCEPTED, transportType, departurePlace);
    }

    @DisplayName("calculateAndSave를 호출할 때,")
    @Nested
    class CalculateAndSave {

        @DisplayName("출발지가 null이면, null을 반환한다.")
        @Test
        void returnsNull_whenDeparturePlaceIsNull() {
            // arrange
            User user = User.create(1L, "유저", "https://example.com/img.jpg");
            Appointment appointment = Appointment.create(
                user, "약속", "강남역", "서울 강남구", 37.4979, 127.0276,
                ZonedDateTime.now().plusDays(1));
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, user, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            assertThat(result).isNull();
            verify(kakaoMobilityApiClient, never()).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }

        @DisplayName("출발지와 목적지 좌표가 동일하면, 외부 API 미호출 후 0분을 저장한다.")
        @Test
        void returnsTravelTimeWithZeroMinutes_whenSameCoordinates() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.WALKING, 37.4979, 127.0276, 37.4979, 127.0276);

            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getDurationMinutes()).isEqualTo(0);
            verify(kakaoMobilityApiClient, never()).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
            verify(odsayApiClient, never()).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }

        @DisplayName("CAR_PARKING 이동수단이면, KakaoMobilityApiClient를 호출한다.")
        @Test
        void callsKakaoMobility_whenCarParking() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.CAR_PARKING, 37.4979, 127.0276, 37.5665, 126.9780);

            given(kakaoMobilityApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(30);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getDurationMinutes()).isEqualTo(30);
            verify(kakaoMobilityApiClient).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }

        @DisplayName("TRANSIT 이동수단이면, OdsayApiClient를 호출한다.")
        @Test
        void callsOdsay_whenTransit() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.TRANSIT, 37.4979, 127.0276, 37.5665, 126.9780);

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(45);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getDurationMinutes()).isEqualTo(45);
            verify(odsayApiClient).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }

        @DisplayName("WALKING 이동수단이면, 외부 API 미호출 후 Haversine 공식으로 계산한다.")
        @Test
        void usesHaversine_whenWalking() {
            // arrange: 약 1km 거리 (약 12~13분 도보)
            AppointmentParticipant participant = makeParticipant(
                TransportType.WALKING, 37.4979, 127.0276, 37.5079, 127.0276);

            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getDurationMinutes()).isGreaterThan(0);
            verify(kakaoMobilityApiClient, never()).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
            verify(odsayApiClient, never()).calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }

        @DisplayName("기존 TravelTime이 있으면, update 후 저장한다.")
        @Test
        void updatesExistingTravelTime_whenAlreadyExists() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.TRANSIT, 37.4979, 127.0276, 37.5665, 126.9780);

            User user = participant.getUser();
            Appointment appointment = participant.getAppointment();
            TravelTime existing = TravelTime.create(participant, 20, TransportType.TRANSIT,
                appointment.getDateTime().minusMinutes(25));

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(35);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.of(existing));
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            assertThat(result).isNotNull();
            assertThat(result.getDurationMinutes()).isEqualTo(35);
        }

        @DisplayName("UserSettings의 extraMinutes가 departureAlertAt 역산에 반영된다.")
        @Test
        void appliesExtraMinutes_fromUserSettings() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.TRANSIT, 37.4979, 127.0276, 37.5665, 126.9780);

            // createDefault: parkingBuffer=10, extraMinutes=5; but TRANSIT → parkingBuffer=0, extra=5
            UserSettings settings = UserSettings.createDefault(participant.getUser());

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(30);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.of(settings));
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            // totalMinutes = 30 + 0(TRANSIT no parking) + 5(extraMinutes) = 35
            ZonedDateTime expected = participant.getAppointment().getDateTime().minusMinutes(35);
            assertThat(result.getDepartureAlertAt()).isEqualToIgnoringNanos(expected);
        }

        @DisplayName("UserSettings가 없으면 extraMinutes 기본값 5분이 적용된다.")
        @Test
        void appliesDefaultExtraMinutes_whenNoUserSettings() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.TRANSIT, 37.4979, 127.0276, 37.5665, 126.9780);

            given(odsayApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(20);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert
            // totalMinutes = 20 + 0 + 5 = 25
            ZonedDateTime expected = participant.getAppointment().getDateTime().minusMinutes(25);
            assertThat(result.getDepartureAlertAt()).isEqualToIgnoringNanos(expected);
        }

        @DisplayName("CAR_PARKING이면, durationMinutes=30이고 departureAlertAt = 약속시간 - 45분(30+주차10+여유5)이다.")
        @Test
        void calculatesCorrectDepartureAlert_forCarParking() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.CAR_PARKING, 37.4979, 127.0276, 37.5665, 126.9780);

            // createDefault: parkingBuffer=10, extraMinutes=5
            UserSettings settings = UserSettings.createDefault(participant.getUser());

            // 카카오모빌리티 API는 분(minutes) 단위로 반환 (duration=1800초 시나리오 → 30분)
            given(kakaoMobilityApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(30);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.of(settings));
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert: durationMinutes=30, departureAlertAt = appointmentTime - 45분(30+주차10+여유5)
            assertThat(result.getDurationMinutes()).isEqualTo(30);
            ZonedDateTime expected = participant.getAppointment().getDateTime().minusMinutes(45);
            assertThat(result.getDepartureAlertAt()).isEqualToIgnoringNanos(expected);
        }

        @DisplayName("CAR_PICKUP이면, durationMinutes=30이고 departureAlertAt = 약속시간 - 35분(30+주차0+여유5)이다.")
        @Test
        void calculatesCorrectDepartureAlert_forCarPickup() {
            // arrange
            AppointmentParticipant participant = makeParticipant(
                TransportType.CAR_PICKUP, 37.4979, 127.0276, 37.5665, 126.9780);

            // createDefault: parkingBuffer=10, extraMinutes=5; CAR_PICKUP은 주차버퍼 미적용
            UserSettings settings = UserSettings.createDefault(participant.getUser());

            // 카카오모빌리티 API는 분(minutes) 단위로 반환 (duration=1800초 시나리오 → 30분)
            given(kakaoMobilityApiClient.calculateDuration(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(30);
            given(userSettingsRepository.findActiveByUserId(any())).willReturn(Optional.of(settings));
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());
            given(travelTimeRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            TravelTime result = travelTimeService.calculateAndSave(participant);

            // assert: durationMinutes=30, departureAlertAt = appointmentTime - 35분(30+주차0+여유5)
            assertThat(result.getDurationMinutes()).isEqualTo(30);
            ZonedDateTime expected = participant.getAppointment().getDateTime().minusMinutes(35);
            assertThat(result.getDepartureAlertAt()).isEqualToIgnoringNanos(expected);
        }
    }
}
