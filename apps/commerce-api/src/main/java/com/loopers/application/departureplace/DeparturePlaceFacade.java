package com.loopers.application.departureplace;

import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.departureplace.DeparturePlaceService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class DeparturePlaceFacade {

    private final DeparturePlaceService departurePlaceService;
    private final UserService userService;

    public List<DeparturePlaceInfo> getMyDeparturePlaces(Long userId) {
        return departurePlaceService.getAllByUserId(userId).stream()
            .map(DeparturePlaceInfo::from)
            .toList();
    }

    public DeparturePlaceInfo create(Long userId, String label, String address,
                                     Double latitude, Double longitude) {
        User user = userService.getUser(userId);
        DeparturePlace place = departurePlaceService.create(user, label, address, latitude, longitude);
        return DeparturePlaceInfo.from(place);
    }

    public DeparturePlaceInfo update(Long userId, Long id, String label, String address,
                                     Double latitude, Double longitude) {
        DeparturePlace place = departurePlaceService.update(id, userId, label, address, latitude, longitude);
        return DeparturePlaceInfo.from(place);
    }

    public void delete(Long userId, Long id) {
        departurePlaceService.delete(id, userId);
    }
}
