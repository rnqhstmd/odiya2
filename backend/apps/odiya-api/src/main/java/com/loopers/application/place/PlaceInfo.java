package com.loopers.application.place;

import com.loopers.infrastructure.kakao.KakaoLocalResponse;

public record PlaceInfo(
    String kakaoPlaceId,
    String name,
    String address,
    String roadAddress,
    String category,
    Double latitude,
    Double longitude
) {
    public static PlaceInfo from(KakaoLocalResponse.Document doc) {
        return new PlaceInfo(
            doc.id(),
            doc.placeName(),
            doc.addressName(),
            doc.roadAddressName(),
            doc.categoryGroupName(),
            parseDoubleSafe(doc.y()),
            parseDoubleSafe(doc.x())
        );
    }

    private static Double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean hasValidCoordinates(PlaceInfo place) {
        return place.latitude() != null && place.longitude() != null
            && place.latitude() != 0.0 && place.longitude() != 0.0;
    }
}
