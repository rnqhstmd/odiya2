package com.loopers.domain.appointment;

import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.user.User;
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
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentParticipantRepository appointmentParticipantRepository;

    @Transactional
    public Appointment create(User host, String name, String placeName, String placeAddress,
                              Double latitude, Double longitude, ZonedDateTime dateTime,
                              TransportType transportType, DeparturePlace departurePlace) {
        if (!dateTime.isAfter(ZonedDateTime.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "약속 일시는 현재 시각 이후여야 합니다.");
        }
        Appointment appointment = Appointment.create(host, name, placeName, placeAddress,
            latitude, longitude, dateTime);
        appointment = appointmentRepository.save(appointment);

        AppointmentParticipant hostParticipant = AppointmentParticipant.create(
            appointment, host, ParticipantStatus.ACCEPTED, transportType, departurePlace);
        appointment.addParticipant(hostParticipant);

        return appointment;
    }

    @Transactional
    public AppointmentParticipant addParticipant(Appointment appointment, User user,
                                                 TransportType transportType) {
        AppointmentParticipant participant = AppointmentParticipant.create(
            appointment, user, ParticipantStatus.PENDING, transportType, null);
        appointmentParticipantRepository.save(participant);
        appointment.addParticipant(participant);
        return participant;
    }

    @Transactional(readOnly = true)
    public Appointment getActiveAppointment(Long id) {
        return appointmentRepository.findActiveById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 약속입니다."));
    }

    @Transactional
    public Appointment update(Long id, Long hostUserId, String name, String placeName,
                              String placeAddress, Double latitude, Double longitude,
                              ZonedDateTime dateTime) {
        Appointment appointment = getActiveAppointment(id);
        validateBeforeAppointmentTime(appointment);
        validateHost(appointment, hostUserId);
        if (dateTime != null && !dateTime.isAfter(ZonedDateTime.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "약속 일시는 현재 시각 이후여야 합니다.");
        }
        appointment.update(name, placeName, placeAddress, latitude, longitude, dateTime);
        return appointment;
    }

    @Transactional
    public void cancel(Long id, Long hostUserId) {
        Appointment appointment = getActiveAppointment(id);
        validateBeforeAppointmentTime(appointment);
        validateHost(appointment, hostUserId);
        appointment.cancel();
    }

    @Transactional
    public AppointmentParticipant acceptInvitation(Long appointmentId, Long userId) {
        Appointment appointment = getActiveAppointment(appointmentId);
        validateBeforeAppointmentTime(appointment);
        AppointmentParticipant participant = appointment.findParticipant(userId);
        participant.accept();
        appointment.confirmIfAllAccepted();
        return participant;
    }

    @Transactional
    public void rejectInvitation(Long appointmentId, Long userId) {
        Appointment appointment = getActiveAppointment(appointmentId);
        validateBeforeAppointmentTime(appointment);
        AppointmentParticipant participant = appointment.findParticipant(userId);
        participant.reject();
        appointment.confirmIfAllAccepted();
    }

    @Transactional
    public AppointmentParticipant updateDeparture(Long appointmentId, Long userId,
                                                  DeparturePlace departurePlace,
                                                  TransportType transportType) {
        Appointment appointment = getActiveAppointment(appointmentId);
        AppointmentParticipant participant = appointment.findParticipant(userId);
        participant.updateDeparture(departurePlace, transportType);
        return participant;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointments(Long userId, Long cursor, int size) {
        return appointmentRepository.findUpcomingByUserId(userId, ZonedDateTime.now(), cursor, size);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getPastAppointments(Long userId, Long cursor, int size) {
        return appointmentRepository.findPastByUserId(userId, ZonedDateTime.now(), cursor, size);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getCalendarAppointments(Long userId, int year, int month) {
        return appointmentRepository.findByUserIdAndMonth(userId, year, month);
    }

    private void validateHost(Appointment appointment, Long userId) {
        if (!appointment.isHostUser(userId)) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "호스트만 이 작업을 수행할 수 있습니다.");
        }
    }

    private void validateBeforeAppointmentTime(Appointment appointment) {
        if (!appointment.getDateTime().isAfter(ZonedDateTime.now())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "약속 시간이 지난 약속은 변경할 수 없습니다.");
        }
    }
}
