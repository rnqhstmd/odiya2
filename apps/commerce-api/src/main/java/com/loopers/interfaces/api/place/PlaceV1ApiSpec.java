package com.loopers.interfaces.api.place;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Place", description = "장소 검색 API")
public interface PlaceV1ApiSpec {

    @Operation(summary = "장소 검색", description = "카카오 로컬 API를 통한 키워드 장소 검색")
    ApiResponse<PlaceV1Dto.SearchResponse> searchPlaces(
        @Parameter(description = "검색어 (장소명/주소)", required = true) String keyword,
        @Parameter(description = "페이지 번호 (기본 1)") Integer page,
        @Parameter(description = "페이지 크기 (기본 15)") Integer size
    );
}
