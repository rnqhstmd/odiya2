package com.loopers.infrastructure.appointment;

import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AppointmentParticipantRepositoryImpl implements AppointmentParticipantRepository {

    private final AppointmentParticipantJpaRepository appointmentParticipantJpaRepository;

    @Override
    public AppointmentParticipant save(AppointmentParticipant participant) {
        return appointmentParticipantJpaRepository.save(participant);
    }

    @Override
    public Optional<AppointmentParticipant> findByAppointmentIdAndUserId(Long appointmentId, Long userId) {
        return appointmentParticipantJpaRepository.findByAppointmentIdAndUserId(appointmentId, userId);
    }
}
