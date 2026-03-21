package com.loopers.infrastructure.appointment;

import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AppointmentRepositoryImpl implements AppointmentRepository {

    private final AppointmentJpaRepository appointmentJpaRepository;

    @Override
    public Appointment save(Appointment appointment) {
        return appointmentJpaRepository.save(appointment);
    }

    @Override
    public Optional<Appointment> findActiveById(Long id) {
        return appointmentJpaRepository.findActiveById(id);
    }

    @Override
    public List<Appointment> findUpcomingByUserId(Long userId, ZonedDateTime now, Long cursor, int size) {
        List<Long> ids = appointmentJpaRepository.findUpcomingIdsByUserId(userId, now, cursor, PageRequest.of(0, size));
        if (ids.isEmpty()) return List.of();
        List<Appointment> appointments = appointmentJpaRepository.findAllWithParticipantsByIds(ids);
        appointments.sort(Comparator.comparingLong(Appointment::getId));
        return appointments;
    }

    @Override
    public List<Appointment> findPastByUserId(Long userId, ZonedDateTime now, Long cursor, int size) {
        List<Long> ids = appointmentJpaRepository.findPastIdsByUserId(userId, now, cursor, PageRequest.of(0, size));
        if (ids.isEmpty()) return List.of();
        List<Appointment> appointments = appointmentJpaRepository.findAllWithParticipantsByIds(ids);
        appointments.sort(Comparator.comparingLong(Appointment::getId).reversed());
        return appointments;
    }

    @Override
    public List<Appointment> findByUserIdAndMonth(Long userId, int year, int month) {
        List<Long> ids = appointmentJpaRepository.findIdsByUserIdAndMonth(userId, year, month);
        if (ids.isEmpty()) return List.of();
        List<Appointment> appointments = appointmentJpaRepository.findAllWithParticipantsByIds(ids);
        appointments.sort(Comparator.comparing(Appointment::getDateTime));
        return appointments;
    }
}
