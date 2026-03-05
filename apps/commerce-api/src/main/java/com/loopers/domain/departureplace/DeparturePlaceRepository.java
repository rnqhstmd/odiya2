package com.loopers.domain.departureplace;

import java.util.List;
import java.util.Optional;

public interface DeparturePlaceRepository {
    DeparturePlace save(DeparturePlace departurePlace);
    Optional<DeparturePlace> findActiveById(Long id);
    List<DeparturePlace> findAllActiveByUserId(Long userId);
    int countActiveByUserId(Long userId);
}
