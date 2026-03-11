package com.loopers.infrastructure.traveltime;

import com.loopers.domain.traveltime.TravelTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelTimeJpaRepository extends JpaRepository<TravelTime, Long> {
    Optional<TravelTime> findByParticipantIdAndDeletedAtIsNull(Long participantId);
}
