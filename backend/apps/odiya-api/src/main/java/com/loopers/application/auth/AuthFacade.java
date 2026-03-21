package com.loopers.application.auth;

import com.loopers.domain.auth.TokenService;
import com.loopers.domain.tag.TagService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.kakao.KakaoApiClient;
import com.loopers.infrastructure.kakao.KakaoUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class AuthFacade {

    private final KakaoApiClient kakaoApiClient;
    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final TagService tagService;

    @Transactional
    public AuthInfo kakaoLogin(String kakaoAccessToken) {
        KakaoUserResponse kakaoUser = kakaoApiClient.fetchUser(kakaoAccessToken);
        boolean isNew = !userRepository.findActiveByKakaoId(kakaoUser.kakaoId()).isPresent();
        User user = userService.findOrCreateUser(kakaoUser.kakaoId(), kakaoUser.nickname(), kakaoUser.profileImageUrl());
        if (isNew) {
            tagService.initDefaultTags(user);
        }
        TokenService.TokenPair tokenPair = tokenService.issueTokens(user.getId());
        return AuthInfo.from(tokenPair);
    }

    public AuthInfo refresh(String refreshToken) {
        TokenService.TokenPair tokenPair = tokenService.rotate(refreshToken);
        return AuthInfo.from(tokenPair);
    }

    public void logout(String refreshToken) {
        tokenService.invalidate(refreshToken);
    }
}
