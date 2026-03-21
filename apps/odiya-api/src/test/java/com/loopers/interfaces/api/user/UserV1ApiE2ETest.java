package com.loopers.interfaces.api.user;

import com.loopers.config.security.JwtProvider;
import com.loopers.domain.user.User;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class GetMe {

        @DisplayName("인증된 사용자이면, 200과 사용자 정보를 반환한다.")
        @Test
        void returnsUserInfo_whenAuthenticated() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));
            HttpHeaders headers = createAuthHeaders(saved.getId());

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange("/api/v1/users/me", HttpMethod.GET, new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().id()).isEqualTo(saved.getId()),
                () -> assertThat(response.getBody().data().nickname()).isEqualTo("홍길동"),
                () -> assertThat(response.getBody().data().profileImageUrl()).isEqualTo("https://example.com/image.jpg")
            );
        }

        @DisplayName("인증이 없으면, 401을 반환한다.")
        @Test
        void returnsUnauthorized_whenNotAuthenticated() {
            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/users/me", HttpMethod.GET, new HttpEntity<>(null), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("PATCH /api/v1/users/me/nickname")
    @Nested
    class UpdateNickname {

        @DisplayName("정상적인 닉네임이면, 200을 반환한다.")
        @Test
        void returnsOk_whenValidNickname() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));
            HttpHeaders headers = createAuthHeaders(saved.getId());
            var request = new UserV1Dto.UpdateNicknameRequest("새닉네임");

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange("/api/v1/users/me/nickname", HttpMethod.PATCH, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().nickname()).isEqualTo("새닉네임")
            );
        }

        @DisplayName("21자 초과 닉네임이면, 400을 반환한다.")
        @Test
        void returnsBadRequest_whenNicknameTooLong() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));
            HttpHeaders headers = createAuthHeaders(saved.getId());
            String longNickname = "가".repeat(21);
            var request = new UserV1Dto.UpdateNicknameRequest(longNickname);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/users/me/nickname", HttpMethod.PATCH, new HttpEntity<>(request, headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("빈 문자열 닉네임이면, 400을 반환한다.")
        @Test
        void returnsBadRequest_whenNicknameIsEmpty() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));
            HttpHeaders headers = createAuthHeaders(saved.getId());
            var request = new UserV1Dto.UpdateNicknameRequest("");

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/users/me/nickname", HttpMethod.PATCH, new HttpEntity<>(request, headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @DisplayName("중복 닉네임이면, 409를 반환한다.")
        @Test
        void returnsConflict_whenNicknameIsDuplicate() {
            // arrange
            userJpaRepository.save(User.create(11111L, "기존닉네임", "https://example.com/a.jpg"));
            User target = userJpaRepository.save(User.create(22222L, "다른닉네임", "https://example.com/b.jpg"));
            HttpHeaders headers = createAuthHeaders(target.getId());
            var request = new UserV1Dto.UpdateNicknameRequest("기존닉네임");

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/users/me/nickname", HttpMethod.PATCH, new HttpEntity<>(request, headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @DisplayName("PATCH /api/v1/users/me/profile-image")
    @Nested
    class UpdateProfileImage {

        @DisplayName("정상적인 URL이면, 200을 반환한다.")
        @Test
        void returnsOk_whenValidUrl() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/old.jpg"));
            HttpHeaders headers = createAuthHeaders(saved.getId());
            var request = new UserV1Dto.UpdateProfileImageRequest("https://example.com/new.jpg");

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UserResponse>> responseType = new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UserResponse>> response =
                testRestTemplate.exchange("/api/v1/users/me/profile-image", HttpMethod.PATCH, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().profileImageUrl()).isEqualTo("https://example.com/new.jpg")
            );
        }
    }

    @DisplayName("DELETE /api/v1/users/me")
    @Nested
    class Withdraw {

        @DisplayName("인증된 사용자이면, 200을 반환한다.")
        @Test
        void returnsOk_whenAuthenticated() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));
            HttpHeaders headers = createAuthHeaders(saved.getId());

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/users/me", HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    private HttpHeaders createAuthHeaders(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
