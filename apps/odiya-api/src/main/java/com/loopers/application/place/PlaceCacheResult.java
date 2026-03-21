package com.loopers.application.place;

public record PlaceCacheResult(
    PlaceSearchResult result,
    boolean cacheHit
) {
}
