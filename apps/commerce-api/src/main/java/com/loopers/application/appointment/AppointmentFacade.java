package com.loopers.application.appointment;

import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentService;
import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.departureplace.DeparturePlaceRepository;
import com.loopers.domain.friend.FriendshipRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class AppointmentFacade {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final FriendshipRepository friendshipRepository;
    private final DeparturePlaceRepository departurePlaceRepository;

    @Transactional
    public AppointmentInfo create(Long hostUserId, String name, String placeName, String placeAddress,
                                  Double latitude, Double longitude, ZonedDateTime dateTime,
                                  List<Long> participantIds, TransportType transportType,
                                  Long departurePlaceId) {
        User host = userService.getUser(hostUserId);

        DeparturePlace departurePlace = null;
        if (departurePlaceId != null) {
            departurePlace = departurePlaceRepository.findActiveById(departurePlaceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다."));
            if (!departurePlace.isOwnedBy(hostUserId)) {
                throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다.");
            }
        }

        Appointment appointment = appointmentService.create(
            host, name, placeName, placeAddress, latitude, longitude, dateTime,
            transportType, departurePlace);

        List<Long> uniqueIds = participantIds.stream().distinct().toList();
        for (Long participantId : uniqueIds) {
            if (participantId.equals(hostUserId)) {
                continue;
            }
            if (!friendshipRepository.existsAcceptedFriendship(hostUserId, participantId)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "친구 관계인 사용자만 초대할 수 있습니다.");
            }
            User participant = userService.getUser(participantId);
            appointmentService.addParticipant(appointment, participant, TransportType.TRANSIT);
        }

        return AppointmentInfo.from(appointment, hostUserId);
    }

    @Transactional(readOnly = true)
    public AppointmentInfo getAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        validateParticipant(appointment, userId);
        return AppointmentInfo.from(appointment, userId);
    }

    @Transactional(readOnly = true)
    public AppointmentDetailInfo getAppointmentDetail(Long appointmentId, Long userId) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        validateParticipant(appointment, userId);
        AppointmentInfo info = AppointmentInfo.from(appointment, userId);

        AppointmentParticipant myParticipant = appointment.findParticipant(userId);

        return new AppointmentDetailInfo(
            info,
            false,   // canNudge - Phase 5
            null,    // nudgeCooldownSeconds - Phase 5
            appointment.isHostUser(userId),
            myParticipant.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public AppointmentListInfo getMyAppointments(Long userId, String status, Long cursor, int size) {
        if (!"UPCOMING".equalsIgnoreCase(status) && !"PAST".equalsIgnoreCase(status)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "status는 UPCOMING 또는 PAST여야 합니다.");
        }
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<Appointment> appointments;
        if ("PAST".equalsIgnoreCase(status)) {
            appointments = appointmentService.getPastAppointments(userId, cursor, safeSize + 1);
        } else {
            appointments = appointmentService.getUpcomingAppointments(userId, cursor, safeSize + 1);
        }

        boolean hasNext = appointments.size() > safeSize;
        if (hasNext) {
            appointments = appointments.subList(0, safeSize);
        }

        List<AppointmentInfo> infos = appointments.stream()
            .map(a -> AppointmentInfo.from(a, userId))
            .toList();

        return new AppointmentListInfo(infos, hasNext);
    }

    @Transactional(readOnly = true)
    public List<CalendarDayInfo> getCalendarData(Long userId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new CoreException(ErrorType.BAD_REQUEST, "월은 1~12 범위여야 합니다.");
        }
        if (year < 2000 || year > 2100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 연도입니다.");
        }
        List<Appointment> appointments = appointmentService.getCalendarAppointments(userId, year, month);

        return appointments.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                a -> a.getDateTime().toLocalDate()))
            .entrySet().stream()
            .sorted(java.util.Map.Entry.comparingByKey())
            .map(entry -> new CalendarDayInfo(
                entry.getKey(),
                entry.getValue().stream()
                    .map(a -> new CalendarAppointmentInfo(
                        a.getId(), a.getName(), a.getDateTime(), a.getPlaceName(), null))
                    .toList()))
            .toList();
    }

    @Transactional
    public AppointmentInfo updateAppointment(Long appointmentId, Long userId, String name,
                                             String placeName, String placeAddress,
                                             Double latitude, Double longitude,
                                             ZonedDateTime dateTime) {
        Appointment appointment = appointmentService.update(
            appointmentId, userId, name, placeName, placeAddress, latitude, longitude, dateTime);
        return AppointmentInfo.from(appointment, userId);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        appointmentService.cancel(appointmentId, userId);
    }

    @Transactional
    public void acceptInvitation(Long appointmentId, Long userId) {
        appointmentService.acceptInvitation(appointmentId, userId);
    }

    @Transactional
    public void rejectInvitation(Long appointmentId, Long userId) {
        appointmentService.rejectInvitation(appointmentId, userId);
    }

    @Transactional
    public void inviteParticipants(Long appointmentId, Long hostUserId, List<Long> userIds) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        if (!appointment.isHostUser(hostUserId)) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "호스트만 추가 초대를 할 수 있습니다.");
        }

        List<Long> uniqueUserIds = userIds.stream().distinct().toList();
        for (Long userId : uniqueUserIds) {
            if (!friendshipRepository.existsAcceptedFriendship(hostUserId, userId)) {
                throw new CoreException(ErrorType.BAD_REQUEST, "친구 관계인 사용자만 초대할 수 있습니다.");
            }
            boolean alreadyParticipant = appointment.getParticipants().stream()
                .anyMatch(p -> p.getUser().getId().equals(userId));
            if (alreadyParticipant) {
                throw new CoreException(ErrorType.CONFLICT, "이미 초대된 참여자입니다.");
            }
            User user = userService.getUser(userId);
            appointmentService.addParticipant(appointment, user, TransportType.TRANSIT);
        }
        appointment.revertToPendingIfConfirmed();
    }

    @Transactional
    public DepartureUpdateInfo updateDeparture(Long appointmentId, Long userId,
                                               Long departurePlaceId, TransportType transportType) {
        DeparturePlace departurePlace = null;
        if (departurePlaceId != null) {
            departurePlace = departurePlaceRepository.findActiveById(departurePlaceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다."));
            if (!departurePlace.isOwnedBy(userId)) {
                throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다.");
            }
        }

        AppointmentParticipant participant = appointmentService.updateDeparture(
            appointmentId, userId, departurePlace, transportType);

        String departurePlaceLabel = participant.getDeparturePlace() != null
            ? participant.getDeparturePlace().getLabel() : null;

        return new DepartureUpdateInfo(
            null,  // durationMinutes - Phase 4
            null,  // departureAlertAt - Phase 4
            participant.getTransportType(),
            departurePlaceLabel
        );
    }

    private void validateParticipant(Appointment appointment, Long userId) {
        boolean isParticipant = appointment.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(userId));
        if (!isParticipant) {
            throw new CoreException(ErrorType.NOT_FOUND, "해당 약속의 참여자가 아닙니다.");
        }
    }
}
