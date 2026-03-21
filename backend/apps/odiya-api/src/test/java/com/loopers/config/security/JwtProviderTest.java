package com.loopers.config.security;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProviderTest {

    private static final String SECRET = "local-test-secret-key-must-be-at-least-32-chars";
    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(SECRET, 3600, 2592000);
        jwtProvider = new JwtProvider(properties);
    }

    @DisplayName("액세스 토큰을 생성하고 파싱할 때,")
    @Nested
    class AccessToken {

        @DisplayName("생성한 토큰을 파싱하면, 동일한 userId를 반환한다.")
        @Test
        void returnsLoginUser_whenValidAccessTokenIsParsed() {
            // arrange
            Long userId = 1L;
            String token = jwtProvider.createAccessToken(userId);

            // act
            LoginUser loginUser = jwtProvider.parseAccessToken(token);

            // assert
            assertThat(loginUser.userId()).isEqualTo(userId);
        }

        @DisplayName("만료된 토큰을 파싱하면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorized_whenTokenIsExpired() {
            // arrange
            JwtProperties expiredProps = new JwtProperties(SECRET, -1, -1);
            JwtProvider expiredProvider = new JwtProvider(expiredProps);
            String expiredToken = expiredProvider.createAccessToken(1L);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> jwtProvider.parseAccessToken(expiredToken));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }

        @DisplayName("위변조된 토큰을 파싱하면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorized_whenTokenIsTampered() {
            // arrange
            String token = jwtProvider.createAccessToken(1L);
            String tamperedToken = token + "tampered";

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> jwtProvider.parseAccessToken(tamperedToken));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    @DisplayName("리프레시 토큰을 생성하고 파싱할 때,")
    @Nested
    class RefreshToken {

        @DisplayName("생성한 토큰을 파싱하면, userId와 tokenId를 반환한다.")
        @Test
        void returnsRefreshTokenClaims_whenValidRefreshTokenIsParsed() {
            // arrange
            Long userId = 1L;
            String tokenId = UUID.randomUUID().toString();
            String token = jwtProvider.createRefreshToken(userId, tokenId);

            // act
            JwtProvider.RefreshTokenClaims claims = jwtProvider.parseRefreshToken(token);

            // assert
            assertAll(
                () -> assertThat(claims.userId()).isEqualTo(userId),
                () -> assertThat(claims.tokenId()).isEqualTo(tokenId)
            );
        }

        @DisplayName("액세스 토큰을 리프레시로 파싱하면, UNAUTHORIZED 예외가 발생한다.")
        @Test
        void throwsUnauthorized_whenAccessTokenIsParsedAsRefresh() {
            // arrange
            String accessToken = jwtProvider.createAccessToken(1L);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> jwtProvider.parseRefreshToken(accessToken));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.UNAUTHORIZED);
        }
    }

    @DisplayName("시크릿 키가 32바이트 미만이면, IllegalArgumentException이 발생한다.")
    @Test
    void throwsException_whenSecretKeyIsTooShort() {
        // arrange
        JwtProperties shortSecretProps = new JwtProperties("short-secret", 3600, 2592000);

        // act & assert
        assertThrows(IllegalArgumentException.class, () -> new JwtProvider(shortSecretProps));
    }
}
