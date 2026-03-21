package com.loopers.domain.place;

import com.loopers.application.place.PlaceSearchResult;

import java.util.Optional;

public interface PlaceCacheRepository {

    Optional<PlaceSearchResult> find(String keyword, int page, int size);

    void save(String keyword, int page, int size, PlaceSearchResult result);
}
