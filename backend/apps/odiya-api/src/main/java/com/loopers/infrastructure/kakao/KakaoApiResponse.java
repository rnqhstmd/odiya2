package com.loopers.infrastructure.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KakaoApiResponse(
    Long id,
    @JsonProperty("kakao_account") KakaoAccount kakaoAccount
) {
    public record KakaoAccount(
        Profile profile
    ) {
        public record Profile(
            String nickname,
            @JsonProperty("profile_image_url") String profileImageUrl
        ) {}
    }

    public String nickname() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().nickname();
    }

    public String profileImageUrl() {
        if (kakaoAccount == null || kakaoAccount.profile() == null) {
            return null;
        }
        return kakaoAccount.profile().profileImageUrl();
    }
}
