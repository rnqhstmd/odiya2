package com.loopers.infrastructure.kakao;

public record KakaoUserResponse(
    Long kakaoId,
    String nickname,
    String profileImageUrl
) {
    public static KakaoUserResponse from(KakaoApiResponse response) {
        return new KakaoUserResponse(
            response.id(),
            response.nickname(),
            response.profileImageUrl()
        );
    }
}
