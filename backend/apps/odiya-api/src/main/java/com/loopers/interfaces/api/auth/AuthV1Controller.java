package com.loopers.interfaces.api.auth;

import com.loopers.application.auth.AuthFacade;
import com.loopers.application.auth.AuthInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthV1Controller implements AuthV1ApiSpec {

    private final AuthFacade authFacade;

    @PostMapping("/kakao/login")
    @Override
    public ApiResponse<AuthV1Dto.TokenResponse> kakaoLogin(
        @Valid @RequestBody AuthV1Dto.KakaoLoginRequest request
    ) {
        AuthInfo authInfo = authFacade.kakaoLogin(request.kakaoAccessToken());
        return ApiResponse.success(AuthV1Dto.TokenResponse.from(authInfo));
    }

    @PostMapping("/token/refresh")
    @Override
    public ApiResponse<AuthV1Dto.TokenResponse> refresh(
        @RequestBody AuthV1Dto.RefreshRequest request
    ) {
        AuthInfo authInfo = authFacade.refresh(request.refreshToken());
        return ApiResponse.success(AuthV1Dto.TokenResponse.from(authInfo));
    }

    @PostMapping("/logout")
    @Override
    public ApiResponse<Void> logout(
        @RequestBody AuthV1Dto.LogoutRequest request
    ) {
        authFacade.logout(request.refreshToken());
        return ApiResponse.<Void>success();
    }
}
