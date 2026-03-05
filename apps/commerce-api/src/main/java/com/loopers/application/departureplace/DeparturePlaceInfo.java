package com.loopers.application.departureplace;

import com.loopers.domain.departureplace.DeparturePlace;

public record DeparturePlaceInfo(Long id, String label, String address,
                                 Double latitude, Double longitude) {
    public static DeparturePlaceInfo from(DeparturePlace place) {
        return new DeparturePlaceInfo(
            place.getId(),
            place.getLabel(),
            place.getAddress(),
            place.getLatitude(),
            place.getLongitude()
        );
    }
}
