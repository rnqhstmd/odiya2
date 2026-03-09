package com.loopers.domain.appointment;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);
    Optional<Appointment> findActiveById(Long id);
    List<Appointment> findUpcomingByUserId(Long userId, ZonedDateTime now, Long cursor, int size);
    List<Appointment> findPastByUserId(Long userId, ZonedDateTime now, Long cursor, int size);
    List<Appointment> findByUserIdAndMonth(Long userId, int year, int month);
}
