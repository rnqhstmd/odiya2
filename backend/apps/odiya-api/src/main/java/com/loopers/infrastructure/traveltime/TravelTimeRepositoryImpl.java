package com.loopers.infrastructure.traveltime;

import com.loopers.domain.traveltime.TravelTime;
import com.loopers.domain.traveltime.TravelTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class TravelTimeRepositoryImpl implements TravelTimeRepository {

    private final TravelTimeJpaRepository jpaRepository;

    @Override
    public TravelTime save(TravelTime travelTime) {
        return jpaRepository.save(travelTime);
    }

    @Override
    public Optional<TravelTime> findByParticipantId(Long participantId) {
        return jpaRepository.findByParticipantIdAndDeletedAtIsNull(participantId);
    }

    @Override
    public void delete(TravelTime travelTime) {
        travelTime.delete();
        jpaRepository.save(travelTime);
    }
}
