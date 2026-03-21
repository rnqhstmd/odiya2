package com.loopers.domain.appointment;

import java.util.Optional;

public interface AppointmentParticipantRepository {
    AppointmentParticipant save(AppointmentParticipant participant);
    Optional<AppointmentParticipant> findById(Long id);
    Optional<AppointmentParticipant> findByAppointmentIdAndUserId(Long appointmentId, Long userId);
}
