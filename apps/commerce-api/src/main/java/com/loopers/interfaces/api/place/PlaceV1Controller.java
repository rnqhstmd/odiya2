package com.loopers.interfaces.api.place;

import com.loopers.application.place.PlaceFacade;
import com.loopers.application.place.PlaceSearchResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/places")
public class PlaceV1Controller implements PlaceV1ApiSpec {

    private final PlaceFacade placeFacade;

    @Override
    @GetMapping("/search")
    public ApiResponse<PlaceV1Dto.SearchResponse> searchPlaces(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "15") Integer size) {
        PlaceSearchResult result = placeFacade.searchPlaces(keyword, page, size);
        return ApiResponse.success(PlaceV1Dto.SearchResponse.from(result));
    }
}
