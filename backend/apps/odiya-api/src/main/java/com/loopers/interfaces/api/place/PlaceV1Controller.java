package com.loopers.interfaces.api.place;

import com.loopers.application.place.PlaceCacheResult;
import com.loopers.application.place.PlaceFacade;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
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
            @RequestParam(defaultValue = "15") Integer size,
            HttpServletResponse httpServletResponse) {
        PlaceCacheResult cacheResult = placeFacade.searchPlaces(keyword, page, size);
        httpServletResponse.addHeader("X-Cache", cacheResult.cacheHit() ? "HIT" : "MISS");
        return ApiResponse.success(PlaceV1Dto.SearchResponse.from(cacheResult.result()));
    }
}
