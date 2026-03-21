package com.loopers.application.appointment;

import com.loopers.application.notification.NotificationFacade;
import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentService;
import com.loopers.domain.appointment.ParticipantStatus;
import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.departureplace.DeparturePlaceRepository;
import com.loopers.domain.friend.FriendService;
import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.nudge.NudgeCooldownRepository;
import com.loopers.domain.traveltime.TravelTime;
import com.loopers.domain.traveltime.TravelTimeCacheRepository;
import com.loopers.domain.traveltime.TravelTimeCalculateEvent;
import com.loopers.domain.traveltime.TravelTimeRepository;
import com.loopers.domain.traveltime.TravelTimeService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.usersettings.TransportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentFacadeCacheTest {

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private UserService userService;

    @Mock
    private FriendService friendService;

    @Mock
    private DeparturePlaceRepository departurePlaceRepository;

    @Mock
    private TravelTimeService travelTimeService;

    @Mock
    private TravelTimeRepository travelTimeRepository;

    @Mock
    private TravelTimeCacheRepository travelTimeCacheRepository;

    @Mock
    private NotificationFacade notificationFacade;

    @Mock
    private NotificationService notificationService;

    @Mock
    private NudgeCooldownRepository nudgeCooldownRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AppointmentFacade appointmentFacade;

    private User makeUser(Long kakaoId) {
        return User.create(kakaoId, "유저", "https://example.com/img.jpg");
    }

    private Appointment makeAppointmentWithParticipant(User host, double lat, double lng,
                                                       ZonedDateTime dateTime,
                                                       TransportType transportType,
                                                       DeparturePlace departurePlace) {
        Appointment appointment = Appointment.create(host, "약속", "장소", "서울 강남구", lat, lng, dateTime);
        AppointmentParticipant participant = AppointmentParticipant.create(
            appointment, host, ParticipantStatus.ACCEPTED, transportType, departurePlace);
        appointment.addParticipant(participant);
        return appointment;
    }

    @DisplayName("acceptInvitation를 호출할 때,")
    @Nested
    class AcceptInvitation {

        @DisplayName("출발지있으면 이벤트를 발행한다.")
        @Test
        void publishesEvent_whenDeparturePlaceExists() {
            // arrange
            User user = makeUser(1L);
            Appointment appointment = Appointment.create(user, "약속", "장소", "서울 강남구", 37.5665, 126.9780,
                ZonedDateTime.now().plusDays(1));
            DeparturePlace departurePlace = DeparturePlace.create(user, "집", "서울 강남구", 37.4979, 127.0276);
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, user, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, departurePlace);

            given(appointmentService.acceptInvitation(anyLong(), anyLong())).willReturn(participant);

            // act
            appointmentFacade.acceptInvitation(1L, 1L);

            // assert
            verify(applicationEventPublisher).publishEvent(any(TravelTimeCalculateEvent.class));
        }

        @DisplayName("출발지없으면 이벤트를 발행하지않는다.")
        @Test
        void doesNotPublishEvent_whenNoDeparturePlace() {
            // arrange
            User user = makeUser(1L);
            Appointment appointment = Appointment.create(user, "약속", "장소", "서울 강남구", 37.5665, 126.9780,
                ZonedDateTime.now().plusDays(1));
            AppointmentParticipant participant = AppointmentParticipant.create(
                appointment, user, ParticipantStatus.ACCEPTED, TransportType.TRANSIT, null);

            given(appointmentService.acceptInvitation(anyLong(), anyLong())).willReturn(participant);

            // act
            appointmentFacade.acceptInvitation(1L, 1L);

            // assert
            verify(applicationEventPublisher, never()).publishEvent(any(TravelTimeCalculateEvent.class));
        }
    }

    @DisplayName("updateAppointment를 호출할 때,")
    @Nested
    class UpdateAppointment {

        @DisplayName("좌표변경시 캐시를 무효화한다.")
        @Test
        void evictsCache_whenCoordinatesChange() {
            // arrange
            User host = makeUser(1L);
            DeparturePlace departurePlace = DeparturePlace.create(host, "집", "서울 강남구", 37.4979, 127.0276);
            Appointment oldAppointment = makeAppointmentWithParticipant(host, 37.5000, 127.0300,
                ZonedDateTime.now().plusDays(3), TransportType.TRANSIT, departurePlace);

            given(appointmentService.getActiveAppointment(anyLong())).willReturn(oldAppointment);
            given(userService.getUser(anyLong())).willReturn(host);
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());

            Appointment updatedAppointment = makeAppointmentWithParticipant(host, 37.5665, 126.9780,
                ZonedDateTime.now().plusDays(3), TransportType.TRANSIT, departurePlace);
            given(appointmentService.update(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(updatedAppointment);

            // act
            appointmentFacade.updateAppointment(1L, host.getId(), null, null, null, 37.5665, 126.9780, null);

            // assert
            verify(travelTimeCacheRepository).evict(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
        }

        @DisplayName("시간변경시 캐시를 무효화한다.")
        @Test
        void evictsCache_whenTimeChanges() {
            // arrange
            User host = makeUser(1L);
            DeparturePlace departurePlace = DeparturePlace.create(host, "집", "서울 강남구", 37.4979, 127.0276);
            ZonedDateTime oldTime = ZonedDateTime.now().plusDays(3);
            Appointment oldAppointment = makeAppointmentWithParticipant(host, 37.5000, 127.0300,
                oldTime, TransportType.TRANSIT, departurePlace);

            given(appointmentService.getActiveAppointment(anyLong())).willReturn(oldAppointment);
            given(userService.getUser(anyLong())).willReturn(host);
            given(travelTimeRepository.findByParticipantId(any())).willReturn(Optional.empty());

            ZonedDateTime newTime = oldTime.plusHours(2);
            Appointment updatedAppointment = makeAppointmentWithParticipant(host, 37.5000, 127.0300,
                newTime, TransportType.TRANSIT, departurePlace);
            given(appointmentService.update(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(updatedAppointment);

            // act
            appointmentFacade.updateAppointment(1L, host.getId(), null, null, null, null, null, newTime);

            // assert
            verify(travelTimeCacheRepository).evict(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
        }

        @DisplayName("변경없으면 캐시를 무효화하지않는다.")
        @Test
        void doesNotEvictCache_whenNothingChanges() {
            // arrange
            User host = makeUser(1L);
            Appointment appointment = makeAppointmentWithParticipant(host, 37.5000, 127.0300,
                ZonedDateTime.now().plusDays(3), TransportType.TRANSIT, null);

            given(appointmentService.getActiveAppointment(anyLong())).willReturn(appointment);
            given(appointmentService.update(any(), any(), any(), any(), any(), any(), any(), any()))
                .willReturn(appointment);

            // act: 이름만 변경
            appointmentFacade.updateAppointment(1L, host.getId(), "새이름", null, null, null, null, null);

            // assert
            verify(travelTimeCacheRepository, never()).evict(anyDouble(), anyDouble(), anyDouble(), anyDouble(), any());
        }
    }
}
