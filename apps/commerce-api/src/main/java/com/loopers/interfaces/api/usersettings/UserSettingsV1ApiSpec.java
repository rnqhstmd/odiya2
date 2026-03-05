package com.loopers.interfaces.api.usersettings;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "UserSettings V1 API", description = "사용자 설정 API")
public interface UserSettingsV1ApiSpec {

    @Operation(summary = "내 설정 조회", description = "인증된 사용자의 설정을 조회합니다. 설정이 없으면 기본값으로 자동 생성합니다.")
    ApiResponse<UserSettingsV1Dto.UserSettingsResponse> getMySettings(LoginUser loginUser);

    @Operation(summary = "내 설정 변경", description = "인증된 사용자의 설정을 부분 수정합니다.")
    ApiResponse<UserSettingsV1Dto.UserSettingsResponse> updateMySettings(
        LoginUser loginUser, UserSettingsV1Dto.UpdateUserSettingsRequest request);
}
