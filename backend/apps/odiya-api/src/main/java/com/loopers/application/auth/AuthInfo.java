package com.loopers.application.auth;

import com.loopers.domain.auth.TokenService;

public record AuthInfo(String accessToken, String refreshToken) {
    public static AuthInfo from(TokenService.TokenPair tokenPair) {
        return new AuthInfo(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}
