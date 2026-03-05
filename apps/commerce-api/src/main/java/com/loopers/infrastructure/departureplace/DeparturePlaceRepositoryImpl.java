package com.loopers.infrastructure.departureplace;

import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.departureplace.DeparturePlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DeparturePlaceRepositoryImpl implements DeparturePlaceRepository {

    private final DeparturePlaceJpaRepository departurePlaceJpaRepository;

    @Override
    public DeparturePlace save(DeparturePlace departurePlace) {
        return departurePlaceJpaRepository.save(departurePlace);
    }

    @Override
    public Optional<DeparturePlace> findActiveById(Long id) {
        return departurePlaceJpaRepository.findActiveById(id);
    }

    @Override
    public List<DeparturePlace> findAllActiveByUserId(Long userId) {
        return departurePlaceJpaRepository.findAllActiveByUserId(userId);
    }

    @Override
    public int countActiveByUserId(Long userId) {
        return departurePlaceJpaRepository.countActiveByUserId(userId);
    }
}
