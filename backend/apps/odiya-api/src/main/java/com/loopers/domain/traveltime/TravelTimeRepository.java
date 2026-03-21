package com.loopers.domain.traveltime;

import java.util.Optional;

public interface TravelTimeRepository {
    TravelTime save(TravelTime travelTime);
    Optional<TravelTime> findByParticipantId(Long participantId);
    void delete(TravelTime travelTime);
}
