package com.loopers.interfaces.api.departureplace;

import com.loopers.application.departureplace.DeparturePlaceFacade;
import com.loopers.application.departureplace.DeparturePlaceInfo;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/departure-places")
public class DeparturePlaceV1Controller implements DeparturePlaceV1ApiSpec {

    private final DeparturePlaceFacade departurePlaceFacade;

    @GetMapping
    @Override
    public ApiResponse<List<DeparturePlaceV1Dto.DeparturePlaceResponse>> getMyDeparturePlaces(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        List<DeparturePlaceInfo> infos = departurePlaceFacade.getMyDeparturePlaces(loginUser.userId());
        List<DeparturePlaceV1Dto.DeparturePlaceResponse> responses = infos.stream()
            .map(DeparturePlaceV1Dto.DeparturePlaceResponse::from)
            .toList();
        return ApiResponse.success(responses);
    }

    @PostMapping
    @Override
    public ApiResponse<DeparturePlaceV1Dto.DeparturePlaceResponse> createDeparturePlace(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody DeparturePlaceV1Dto.CreateDeparturePlaceRequest request
    ) {
        DeparturePlaceInfo info = departurePlaceFacade.create(
            loginUser.userId(), request.label(), request.address(),
            request.latitude(), request.longitude());
        return ApiResponse.success(DeparturePlaceV1Dto.DeparturePlaceResponse.from(info));
    }

    @PatchMapping("/{id}")
    @Override
    public ApiResponse<DeparturePlaceV1Dto.DeparturePlaceResponse> updateDeparturePlace(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id,
        @Valid @RequestBody DeparturePlaceV1Dto.UpdateDeparturePlaceRequest request
    ) {
        DeparturePlaceInfo info = departurePlaceFacade.update(
            loginUser.userId(), id, request.label(), request.address(),
            request.latitude(), request.longitude());
        return ApiResponse.success(DeparturePlaceV1Dto.DeparturePlaceResponse.from(info));
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> deleteDeparturePlace(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id
    ) {
        departurePlaceFacade.delete(loginUser.userId(), id);
        return ApiResponse.success();
    }
}
