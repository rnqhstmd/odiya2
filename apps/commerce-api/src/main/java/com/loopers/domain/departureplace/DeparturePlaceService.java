package com.loopers.domain.departureplace;

import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DeparturePlaceService {

    private static final int MAX_DEPARTURE_PLACES = 10;

    private final DeparturePlaceRepository departurePlaceRepository;

    @Transactional(readOnly = true)
    public List<DeparturePlace> getAllByUserId(Long userId) {
        return departurePlaceRepository.findAllActiveByUserId(userId);
    }

    @Transactional
    public DeparturePlace create(User user, String label, String address,
                                 Double latitude, Double longitude) {
        if (departurePlaceRepository.countActiveByUserId(user.getId()) >= MAX_DEPARTURE_PLACES) {
            throw new CoreException(ErrorType.BAD_REQUEST,
                "출발지는 최대 " + MAX_DEPARTURE_PLACES + "개까지 등록할 수 있습니다.");
        }
        DeparturePlace departurePlace = DeparturePlace.create(user, label, address, latitude, longitude);
        return departurePlaceRepository.save(departurePlace);
    }

    @Transactional
    public DeparturePlace update(Long id, Long userId, String label, String address,
                                 Double latitude, Double longitude) {
        DeparturePlace place = getActiveOwnedPlace(id, userId);
        place.update(label, address, latitude, longitude);
        return departurePlaceRepository.save(place);
    }

    @Transactional
    public void delete(Long id, Long userId) {
        DeparturePlace place = getActiveOwnedPlace(id, userId);
        place.delete();
        departurePlaceRepository.save(place);
    }

    private DeparturePlace getActiveOwnedPlace(Long id, Long userId) {
        DeparturePlace place = departurePlaceRepository.findActiveById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다."));
        if (!place.isOwnedBy(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 출발지입니다.");
        }
        return place;
    }
}
