package com.loopers.application.appointment;

import com.loopers.application.common.ErrorMessages;
import com.loopers.application.notification.NotificationFacade;
import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentService;
import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.departureplace.DeparturePlaceRepository;
import com.loopers.domain.friend.FriendService;
import com.loopers.domain.notification.NotificationService;
import com.loopers.domain.notification.NotificationType;
import com.loopers.domain.nudge.NudgeCooldownRepository;
import com.loopers.domain.traveltime.TravelTime;
import com.loopers.domain.traveltime.TravelTimeCacheRepository;
import com.loopers.domain.traveltime.TravelTimeCalculateEvent;
import com.loopers.domain.traveltime.TravelTimeRepository;
import com.loopers.domain.traveltime.TravelTimeService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class AppointmentFacade {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final FriendService friendService;
    private final DeparturePlaceRepository departurePlaceRepository;
    private final TravelTimeService travelTimeService;
    private final TravelTimeRepository travelTimeRepository;
    private final TravelTimeCacheRepository travelTimeCacheRepository;
    private final NotificationFacade notificationFacade;
    private final NotificationService notificationService;
    private final NudgeCooldownRepository nudgeCooldownRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public AppointmentInfo create(Long hostUserId, String name, String placeName, String placeAddress,
                                  Double latitude, Double longitude, ZonedDateTime dateTime,
                                  List<Long> participantIds, TransportType transportType,
                                  Long departurePlaceId) {
        User host = userService.getUser(hostUserId);

        DeparturePlace departurePlace = null;
        if (departurePlaceId != null) {
            departurePlace = departurePlaceRepository.findActiveById(departurePlaceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, ErrorMessages.DEPARTURE_PLACE_NOT_FOUND));
            if (!departurePlace.isOwnedBy(hostUserId)) {
                throw new CoreException(ErrorType.NOT_FOUND, ErrorMessages.DEPARTURE_PLACE_NOT_FOUND);
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
            if (!friendService.isAcceptedFriend(hostUserId, participantId)) {
                throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessages.INVITE_REQUIRES_FRIENDSHIP);
            }
            User participant = userService.getUser(participantId);
            appointmentService.addParticipant(appointment, participant, TransportType.TRANSIT);
        }

        Integer durationMinutes = null;
        ZonedDateTime departureAlertAt = null;
        if (departurePlace != null) {
            try {
                AppointmentParticipant hostParticipant = appointment.findParticipant(hostUserId);
                TravelTime travelTime = travelTimeService.calculateAndSave(hostParticipant);
                if (travelTime != null) {
                    durationMinutes = travelTime.getDurationMinutes();
                    departureAlertAt = travelTime.getDepartureAlertAt();
                }
            } catch (Exception e) {
                log.warn("이동시간 계산 실패 (약속 생성): appointmentId={}, error={}", appointment.getId(), e.getMessage(), e);
            }
        }

        AppointmentInfo result = AppointmentInfo.from(appointment, hostUserId, durationMinutes, departureAlertAt);

        for (AppointmentParticipant p : appointment.getParticipants()) {
            if (p.getUser().getId().equals(hostUserId)) {
                continue;
            }
            sendAppointmentNotification(p.getUser(), host, NotificationType.APPOINTMENT_INVITE,
                    host.getNickname() + "님이 약속에 초대했어요", appointment.getName(), appointment.getId());
        }

        return result;
    }

    @Transactional(readOnly = true)
    public AppointmentInfo getAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        validateParticipant(appointment, userId);
        return buildAppointmentInfoWithTravelTime(appointment, userId);
    }

    @Transactional(readOnly = true)
    public AppointmentDetailInfo getAppointmentDetail(Long appointmentId, Long userId) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        AppointmentParticipant myParticipant = appointment.findParticipant(userId);
        AppointmentInfo info = buildAppointmentInfoWithTravelTime(appointment, userId);

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime appointmentTime = appointment.getDateTime();
        boolean inNudgeWindow = !now.isBefore(appointmentTime.minusMinutes(30)) && now.isBefore(appointmentTime);

        boolean canNudge = false;
        if (inNudgeWindow) {
            List<AppointmentParticipant> others = appointment.getParticipants().stream()
                .filter(p -> !p.getUser().getId().equals(userId))
                .toList();
            boolean anyAvailable = others.stream()
                .anyMatch(p -> !nudgeCooldownRepository.existsCooldown(appointmentId, userId, p.getUser().getId()));
            canNudge = anyAvailable;
        }

        return new AppointmentDetailInfo(
            info,
            canNudge,
            null,
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
            .map(a -> buildAppointmentInfoWithTravelTime(a, userId))
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
            .collect(Collectors.groupingBy(
                a -> a.getDateTime().toLocalDate()))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
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
        Appointment beforeUpdate = appointmentService.getActiveAppointment(appointmentId);
        validateParticipant(beforeUpdate, userId);

        if (name == null && placeName == null && placeAddress == null && latitude == null && longitude == null && dateTime == null) {
            return buildAppointmentInfoWithTravelTime(beforeUpdate, userId);
        }
        Double oldLat = beforeUpdate.getLatitude();
        Double oldLng = beforeUpdate.getLongitude();
        ZonedDateTime oldDateTime = beforeUpdate.getDateTime();

        Appointment appointment = appointmentService.update(
            appointmentId, userId, name, placeName, placeAddress, latitude, longitude, dateTime);

        boolean coordChanged = (latitude != null && !Objects.equals(latitude, oldLat))
            || (longitude != null && !Objects.equals(longitude, oldLng));
        boolean timeChanged = dateTime != null && !dateTime.isEqual(oldDateTime);

        if (coordChanged || timeChanged) {
            User host = userService.getUser(userId);
            List<Long> asyncParticipantIds = new java.util.ArrayList<>();

            for (AppointmentParticipant p : appointment.getParticipants()) {
                if (!p.getUser().getId().equals(userId)) {
                    sendAppointmentNotification(p.getUser(), host, NotificationType.APPOINTMENT_UPDATED,
                            appointment.getName() + " 약속이 변경되었어요",
                            coordChanged ? "장소가 변경되었습니다." : "시간이 변경되었습니다.",
                            appointment.getId());
                }

                if (p.getDeparturePlace() != null) {
                    // Evict cache on coordinate or time change
                    if (coordChanged) {
                        try {
                            travelTimeCacheRepository.evict(
                                p.getDeparturePlace().getLatitude(), p.getDeparturePlace().getLongitude(),
                                oldLat, oldLng, p.getTransportType());
                        } catch (Exception e) {
                            log.warn("이동시간 캐시 삭제 실패 (좌표 변경): participantId={}, error={}", p.getId(), e.getMessage(), e);
                        }
                    } else if (timeChanged) {
                        try {
                            travelTimeCacheRepository.evict(
                                p.getDeparturePlace().getLatitude(), p.getDeparturePlace().getLongitude(),
                                appointment.getLatitude(), appointment.getLongitude(), p.getTransportType());
                        } catch (Exception e) {
                            log.warn("이동시간 캐시 삭제 실패 (시간 변경): participantId={}, error={}", p.getId(), e.getMessage(), e);
                        }
                    }

                    if (p.getUser().getId().equals(userId)) {
                        // Host: synchronous calculation
                        Integer oldDuration = travelTimeRepository.findByParticipantId(p.getId())
                            .map(TravelTime::getDurationMinutes)
                            .orElse(null);

                        try {
                            TravelTime newTravelTime = travelTimeService.calculateAndSave(p);
                            if (newTravelTime != null && oldDuration != null) {
                                Integer newDuration = newTravelTime.getDurationMinutes();
                                long diffMinutes = Math.abs(newDuration - oldDuration);
                                boolean appointmentFarEnough = appointment.getDateTime().isAfter(ZonedDateTime.now().plusMinutes(30));
                                if (diffMinutes >= 5 && appointmentFarEnough) {
                                    sendAppointmentNotification(p.getUser(), null, NotificationType.TRAVEL_TIME_CHANGED,
                                            appointment.getName() + " 이동시간이 변경되었어요",
                                            "이동시간이 " + diffMinutes + "분 변경되었습니다.",
                                            appointment.getId());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("이동시간 계산 실패 (약속 수정): appointmentId={}, participantId={}, error={}",
                                appointmentId, p.getId(), e.getMessage(), e);
                        }
                    } else {
                        // Others: async calculation via event
                        asyncParticipantIds.add(p.getId());
                    }
                }
            }

            if (!asyncParticipantIds.isEmpty()) {
                applicationEventPublisher.publishEvent(new TravelTimeCalculateEvent(asyncParticipantIds));
            }
        }

        return buildAppointmentInfoWithTravelTime(appointment, userId);
    }

    @Transactional
    public void cancelAppointment(Long appointmentId, Long userId) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        User host = userService.getUser(userId);
        List<AppointmentParticipant> participants = appointment.getParticipants();

        appointmentService.cancel(appointmentId, userId);

        notificationService.cancelPendingByAppointmentId(appointmentId);

        for (AppointmentParticipant p : participants) {
            if (p.getUser().getId().equals(userId)) {
                continue;
            }
            sendAppointmentNotification(p.getUser(), host, NotificationType.APPOINTMENT_CANCELLED,
                    appointment.getName() + " 약속이 취소되었어요",
                    host.getNickname() + "님이 약속을 취소했습니다.", appointment.getId());
        }
    }

    @Transactional
    public void acceptInvitation(Long appointmentId, Long userId) {
        AppointmentParticipant participant = appointmentService.acceptInvitation(appointmentId, userId);
        if (participant.getDeparturePlace() != null) {
            applicationEventPublisher.publishEvent(
                new TravelTimeCalculateEvent(List.of(participant.getId())));
        }
    }

    @Transactional
    public void rejectInvitation(Long appointmentId, Long userId) {
        appointmentService.rejectInvitation(appointmentId, userId);
    }

    @Transactional
    public void inviteParticipants(Long appointmentId, Long hostUserId, List<Long> userIds) {
        Appointment appointment = appointmentService.getActiveAppointment(appointmentId);
        if (!appointment.isHostUser(hostUserId)) {
            throw new CoreException(ErrorType.FORBIDDEN, "호스트만 추가 초대를 할 수 있습니다.");
        }

        User host = userService.getUser(hostUserId);

        Set<Long> existingParticipantIds = appointment.getParticipants().stream()
            .map(p -> p.getUser().getId())
            .collect(Collectors.toSet());

        List<Long> uniqueUserIds = userIds.stream().distinct().toList();
        for (Long userId : uniqueUserIds) {
            if (existingParticipantIds.contains(userId)) {
                throw new CoreException(ErrorType.CONFLICT, "이미 초대된 참여자입니다.");
            }
            if (!friendService.isAcceptedFriend(hostUserId, userId)) {
                throw new CoreException(ErrorType.BAD_REQUEST, ErrorMessages.INVITE_REQUIRES_FRIENDSHIP);
            }
            User user = userService.getUser(userId);
            appointmentService.addParticipant(appointment, user, TransportType.TRANSIT);

            sendAppointmentNotification(user, host, NotificationType.APPOINTMENT_INVITE,
                    host.getNickname() + "님이 약속에 초대했어요", appointment.getName(), appointment.getId());
        }
        appointment.revertToPendingIfConfirmed();
    }

    @Transactional
    public DepartureUpdateInfo updateDeparture(Long appointmentId, Long userId,
                                               Long departurePlaceId, TransportType transportType) {
        DeparturePlace departurePlace = null;
        if (departurePlaceId != null) {
            departurePlace = departurePlaceRepository.findActiveById(departurePlaceId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, ErrorMessages.DEPARTURE_PLACE_NOT_FOUND));
            if (!departurePlace.isOwnedBy(userId)) {
                throw new CoreException(ErrorType.NOT_FOUND, ErrorMessages.DEPARTURE_PLACE_NOT_FOUND);
            }
        }

        AppointmentParticipant participant = appointmentService.updateDeparture(
            appointmentId, userId, departurePlace, transportType);

        Integer durationMinutes = null;
        ZonedDateTime departureAlertAt = null;
        if (departurePlace == null) {
            travelTimeRepository.findByParticipantId(participant.getId())
                .ifPresent(travelTimeRepository::delete);
        } else {
            try {
                TravelTime travelTime = travelTimeService.calculateAndSave(participant);
                if (travelTime != null) {
                    durationMinutes = travelTime.getDurationMinutes();
                    departureAlertAt = travelTime.getDepartureAlertAt();
                }
            } catch (Exception e) {
                log.warn("이동시간 계산 실패 (출발지 수정): appointmentId={}, userId={}, error={}", appointmentId, userId, e.getMessage(), e);
            }
        }

        String departurePlaceLabel = participant.getDeparturePlace() != null
            ? participant.getDeparturePlace().getLabel() : null;

        return new DepartureUpdateInfo(
            durationMinutes,
            departureAlertAt,
            participant.getTransportType(),
            departurePlaceLabel
        );
    }

    private AppointmentInfo buildAppointmentInfoWithTravelTime(Appointment appointment, Long userId) {
        AppointmentParticipant currentParticipant = appointment.getParticipants().stream()
            .filter(p -> p.getUser().getId().equals(userId))
            .findFirst()
            .orElse(null);

        Integer durationMinutes = null;
        ZonedDateTime departureAlertAt = null;
        if (currentParticipant != null) {
            TravelTime travelTime = travelTimeRepository.findByParticipantId(currentParticipant.getId())
                .orElse(null);
            if (travelTime != null) {
                durationMinutes = travelTime.getDurationMinutes();
                departureAlertAt = travelTime.getDepartureAlertAt();
            }
        }

        return AppointmentInfo.from(appointment, userId, durationMinutes, departureAlertAt);
    }

    private void sendAppointmentNotification(User receiver, User sender, NotificationType type,
                                                String title, String body, Long appointmentId) {
        try {
            notificationFacade.sendNotification(receiver, sender, type, title, body, appointmentId, "APPOINTMENT");
        } catch (Exception e) {
            log.warn("알림 발송 실패: type={}, appointmentId={}, receiverId={}, error={}",
                type, appointmentId, receiver.getId(), e.getMessage(), e);
        }
    }

    private void validateParticipant(Appointment appointment, Long userId) {
        boolean isParticipant = appointment.getParticipants().stream()
            .anyMatch(p -> p.getUser().getId().equals(userId));
        if (!isParticipant) {
            throw new CoreException(ErrorType.NOT_FOUND, ErrorMessages.APPOINTMENT_PARTICIPANT_NOT_FOUND);
        }
    }
}
