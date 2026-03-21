package com.loopers.interfaces.api.usersettings;

import com.loopers.application.usersettings.UserSettingsFacade;
import com.loopers.application.usersettings.UserSettingsInfo;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users/me/settings")
public class UserSettingsV1Controller implements UserSettingsV1ApiSpec {

    private final UserSettingsFacade userSettingsFacade;

    @GetMapping
    @Override
    public ApiResponse<UserSettingsV1Dto.UserSettingsResponse> getMySettings(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        UserSettingsInfo info = userSettingsFacade.getMySettings(loginUser.userId());
        return ApiResponse.success(UserSettingsV1Dto.UserSettingsResponse.from(info));
    }

    @PatchMapping
    @Override
    public ApiResponse<UserSettingsV1Dto.UserSettingsResponse> updateMySettings(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody UserSettingsV1Dto.UpdateUserSettingsRequest request
    ) {
        UserSettingsInfo info = userSettingsFacade.updateMySettings(
            loginUser.userId(), request.defaultTransportType(),
            request.parkingBufferMinutes(), request.extraMinutes());
        return ApiResponse.success(UserSettingsV1Dto.UserSettingsResponse.from(info));
    }
}
