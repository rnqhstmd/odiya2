package com.loopers.domain.appointment;

import java.util.Optional;

public interface AppointmentParticipantRepository {
    AppointmentParticipant save(AppointmentParticipant participant);
    Optional<AppointmentParticipant> findByAppointmentIdAndUserId(Long appointmentId, Long userId);
}
