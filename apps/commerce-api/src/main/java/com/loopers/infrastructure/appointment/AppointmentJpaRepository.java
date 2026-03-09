package com.loopers.infrastructure.appointment;

import com.loopers.domain.appointment.Appointment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentJpaRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.id = :id AND a.deletedAt IS NULL")
    Optional<Appointment> findActiveById(@Param("id") Long id);

    @Query("SELECT DISTINCT a.id FROM Appointment a JOIN a.participants p " +
        "WHERE p.user.id = :userId AND p.status <> 'REJECTED' " +
        "AND a.dateTime >= :now AND a.deletedAt IS NULL " +
        "AND (:cursor IS NULL OR a.id > :cursor) " +
        "ORDER BY a.id ASC")
    List<Long> findUpcomingIdsByUserId(@Param("userId") Long userId,
                                       @Param("now") ZonedDateTime now,
                                       @Param("cursor") Long cursor,
                                       Pageable pageable);

    @Query("SELECT DISTINCT a.id FROM Appointment a JOIN a.participants p " +
        "WHERE p.user.id = :userId AND p.status <> 'REJECTED' " +
        "AND a.dateTime < :now AND a.deletedAt IS NULL " +
        "AND (:cursor IS NULL OR a.id < :cursor) " +
        "ORDER BY a.id DESC")
    List<Long> findPastIdsByUserId(@Param("userId") Long userId,
                                   @Param("now") ZonedDateTime now,
                                   @Param("cursor") Long cursor,
                                   Pageable pageable);

    @Query("SELECT DISTINCT a.id FROM Appointment a JOIN a.participants p " +
        "WHERE p.user.id = :userId AND p.status IN ('ACCEPTED', 'PENDING') " +
        "AND a.deletedAt IS NULL " +
        "AND YEAR(a.dateTime) = :year AND MONTH(a.dateTime) = :month " +
        "ORDER BY a.id ASC")
    List<Long> findIdsByUserIdAndMonth(@Param("userId") Long userId,
                                       @Param("year") int year,
                                       @Param("month") int month);

    @Query("SELECT DISTINCT a FROM Appointment a " +
        "JOIN FETCH a.participants p JOIN FETCH p.user JOIN FETCH a.host " +
        "WHERE a.id IN :ids")
    List<Appointment> findAllWithParticipantsByIds(@Param("ids") List<Long> ids);
}
