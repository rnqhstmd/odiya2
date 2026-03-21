package com.loopers.infrastructure.appointment;

import com.loopers.domain.appointment.AppointmentParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AppointmentParticipantJpaRepository extends JpaRepository<AppointmentParticipant, Long> {

    @Query("SELECT p FROM AppointmentParticipant p WHERE p.appointment.id = :appointmentId AND p.user.id = :userId AND p.deletedAt IS NULL")
    Optional<AppointmentParticipant> findByAppointmentIdAndUserId(@Param("appointmentId") Long appointmentId,
                                                                  @Param("userId") Long userId);
}
