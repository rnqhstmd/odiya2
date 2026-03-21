package com.loopers.interfaces.api.place;

import com.loopers.application.place.PlaceInfo;
import com.loopers.application.place.PlaceSearchResult;

import java.util.List;

public class PlaceV1Dto {

    public record SearchResponse(
        List<PlaceResponse> places,
        boolean hasNext
    ) {
        public static SearchResponse from(PlaceSearchResult result) {
            List<PlaceResponse> placeResponses = result.places().stream()
                .map(PlaceResponse::from)
                .toList();
            return new SearchResponse(placeResponses, result.hasNext());
        }
    }

    public record PlaceResponse(
        String kakaoPlaceId,
        String name,
        String address,
        String roadAddress,
        String category,
        Double latitude,
        Double longitude
    ) {
        public static PlaceResponse from(PlaceInfo info) {
            return new PlaceResponse(
                info.kakaoPlaceId(),
                info.name(),
                info.address(),
                info.roadAddress(),
                info.category(),
                info.latitude(),
                info.longitude()
            );
        }
    }
}
