package com.loopers.interfaces.api.auth;

import com.loopers.application.auth.AuthInfo;
import jakarta.validation.constraints.NotBlank;

public class AuthV1Dto {

    public record KakaoLoginRequest(@NotBlank String kakaoAccessToken) {}

    public record RefreshRequest(String refreshToken) {}

    public record LogoutRequest(String refreshToken) {}

    public record TokenResponse(String accessToken, String refreshToken) {
        public static TokenResponse from(AuthInfo info) {
            return new TokenResponse(info.accessToken(), info.refreshToken());
        }
    }
}
