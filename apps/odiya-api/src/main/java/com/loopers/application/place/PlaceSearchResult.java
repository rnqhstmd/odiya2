package com.loopers.application.place;

import java.util.List;

public record PlaceSearchResult(
    List<PlaceInfo> places,
    boolean hasNext
) {}
