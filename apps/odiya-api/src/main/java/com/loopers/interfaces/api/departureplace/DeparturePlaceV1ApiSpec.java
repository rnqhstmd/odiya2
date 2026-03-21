package com.loopers.interfaces.api.departureplace;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "DeparturePlace V1 API", description = "출발지 관리 API")
public interface DeparturePlaceV1ApiSpec {

    @Operation(summary = "내 출발지 목록 조회", description = "인증된 사용자의 출발지 목록을 조회합니다.")
    ApiResponse<List<DeparturePlaceV1Dto.DeparturePlaceResponse>> getMyDeparturePlaces(LoginUser loginUser);

    @Operation(summary = "출발지 등록", description = "새로운 출발지를 등록합니다.")
    ApiResponse<DeparturePlaceV1Dto.DeparturePlaceResponse> createDeparturePlace(
        LoginUser loginUser, DeparturePlaceV1Dto.CreateDeparturePlaceRequest request);

    @Operation(summary = "출발지 수정", description = "기존 출발지를 부분 수정합니다.")
    ApiResponse<DeparturePlaceV1Dto.DeparturePlaceResponse> updateDeparturePlace(
        LoginUser loginUser, Long id, DeparturePlaceV1Dto.UpdateDeparturePlaceRequest request);

    @Operation(summary = "출발지 삭제", description = "출발지를 삭제합니다.")
    ApiResponse<Void> deleteDeparturePlace(LoginUser loginUser, Long id);
}
