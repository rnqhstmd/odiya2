package com.loopers.interfaces.api.auth;

import com.loopers.config.security.JwtProperties;
import com.loopers.config.security.JwtProvider;
import com.loopers.infrastructure.kakao.KakaoApiClient;
import com.loopers.infrastructure.kakao.KakaoUserResponse;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private KakaoApiClient kakaoApiClient;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/auth/kakao/login")
    @Nested
    class KakaoLogin {

        @DisplayName("정상적인 카카오 토큰이면, 200과 토큰쌍을 반환한다.")
        @Test
        void returnsTokens_whenValidKakaoToken() {
            // arrange
            when(kakaoApiClient.fetchUser("valid-kakao-token"))
                .thenReturn(new KakaoUserResponse(12345L, "테스트유저", "https://example.com/profile.jpg"));

            var request = new AuthV1Dto.KakaoLoginRequest("valid-kakao-token");

            // act
            ParameterizedTypeReference<ApiResponse<AuthV1Dto.TokenResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<AuthV1Dto.TokenResponse>> response =
                testRestTemplate.exchange("/api/v1/auth/kakao/login", HttpMethod.POST, new HttpEntity<>(request), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().accessToken()).isNotBlank(),
                () -> assertThat(response.getBody().data().refreshToken()).isNotBlank()
            );
        }

        @DisplayName("카카오 인증에 실패하면, 401을 반환한다.")
        @Test
        void returnsUnauthorized_whenKakaoAuthFails() {
            // arrange
            when(kakaoApiClient.fetchUser("invalid-kakao-token"))
                .thenThrow(new CoreException(ErrorType.UNAUTHORIZED, "카카오 인증에 실패했습니다."));

            var request = new AuthV1Dto.KakaoLoginRequest("invalid-kakao-token");

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/auth/kakao/login", HttpMethod.POST, new HttpEntity<>(request), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("POST /api/v1/auth/token/refresh")
    @Nested
    class TokenRefresh {

        @DisplayName("유효한 리프레시 토큰이면, 200과 새 토큰쌍을 반환한다.")
        @Test
        void returnsNewTokens_whenValidRefreshToken() {
            // arrange: 먼저 로그인하여 토큰을 발급받는다
            when(kakaoApiClient.fetchUser("valid-kakao-token"))
                .thenReturn(new KakaoUserResponse(12345L, "테스트유저", "https://example.com/profile.jpg"));

            var loginRequest = new AuthV1Dto.KakaoLoginRequest("valid-kakao-token");
            ParameterizedTypeReference<ApiResponse<AuthV1Dto.TokenResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<AuthV1Dto.TokenResponse>> loginResponse =
                testRestTemplate.exchange("/api/v1/auth/kakao/login", HttpMethod.POST, new HttpEntity<>(loginRequest), responseType);

            String refreshToken = loginResponse.getBody().data().refreshToken();
            var refreshRequest = new AuthV1Dto.RefreshRequest(refreshToken);

            // act
            ResponseEntity<ApiResponse<AuthV1Dto.TokenResponse>> response =
                testRestTemplate.exchange("/api/v1/auth/token/refresh", HttpMethod.POST, new HttpEntity<>(refreshRequest), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().accessToken()).isNotBlank(),
                () -> assertThat(response.getBody().data().refreshToken()).isNotBlank()
            );
        }

        @DisplayName("만료된 리프레시 토큰이면, 401을 반환한다.")
        @Test
        void returnsUnauthorized_whenRefreshTokenIsExpired() {
            // arrange
            String expiredRefreshToken = createExpiredRefreshToken(1L);
            var request = new AuthV1Dto.RefreshRequest(expiredRefreshToken);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/auth/token/refresh", HttpMethod.POST, new HttpEntity<>(request), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("POST /api/v1/auth/logout")
    @Nested
    class Logout {

        @DisplayName("유효한 리프레시 토큰이면, 200을 반환한다.")
        @Test
        void returnsOk_whenValidRefreshToken() {
            // arrange: 먼저 로그인하여 토큰을 발급받는다
            when(kakaoApiClient.fetchUser("valid-kakao-token"))
                .thenReturn(new KakaoUserResponse(12345L, "테스트유저", "https://example.com/profile.jpg"));

            var loginRequest = new AuthV1Dto.KakaoLoginRequest("valid-kakao-token");
            ParameterizedTypeReference<ApiResponse<AuthV1Dto.TokenResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<AuthV1Dto.TokenResponse>> loginResponse =
                testRestTemplate.exchange("/api/v1/auth/kakao/login", HttpMethod.POST, new HttpEntity<>(loginRequest), responseType);

            String refreshToken = loginResponse.getBody().data().refreshToken();
            var logoutRequest = new AuthV1Dto.LogoutRequest(refreshToken);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/auth/logout", HttpMethod.POST, new HttpEntity<>(logoutRequest), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @DisplayName("만료된 토큰으로도, 200을 반환한다.")
        @Test
        void returnsOk_whenRefreshTokenIsExpired() {
            // arrange
            String expiredRefreshToken = createExpiredRefreshToken(1L);
            var request = new AuthV1Dto.LogoutRequest(expiredRefreshToken);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/auth/logout", HttpMethod.POST, new HttpEntity<>(request), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    private String createExpiredRefreshToken(Long userId) {
        JwtProperties expiredProps = new JwtProperties(
            "local-test-secret-key-must-be-at-least-32-chars", -1, -1
        );
        JwtProvider expiredProvider = new JwtProvider(expiredProps);
        return expiredProvider.createRefreshToken(userId, UUID.randomUUID().toString());
    }
}
