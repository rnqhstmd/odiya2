package com.loopers.interfaces.api.auth;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Auth V1 API", description = "카카오 로그인 및 토큰 관리 API")
public interface AuthV1ApiSpec {

    @Operation(
        summary = "카카오 로그인",
        description = "카카오 Access Token으로 로그인합니다. 신규 사용자는 자동 가입됩니다."
    )
    ApiResponse<AuthV1Dto.TokenResponse> kakaoLogin(AuthV1Dto.KakaoLoginRequest request);

    @Operation(
        summary = "토큰 갱신",
        description = "유효한 Refresh Token으로 새 Access Token과 Refresh Token을 발급합니다."
    )
    ApiResponse<AuthV1Dto.TokenResponse> refresh(AuthV1Dto.RefreshRequest request);

    @Operation(
        summary = "로그아웃",
        description = "현재 기기의 Refresh Token을 무효화합니다."
    )
    ApiResponse<Void> logout(AuthV1Dto.LogoutRequest request);
}
