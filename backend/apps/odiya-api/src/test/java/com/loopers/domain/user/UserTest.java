package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

    @DisplayName("사용자를 생성할 때,")
    @Nested
    class Create {

        @DisplayName("정상적인 정보가 주어지면, 정상적으로 생성된다.")
        @Test
        void createsUser_whenValidInfoIsProvided() {
            // arrange
            Long kakaoId = 12345L;
            String nickname = "홍길동";
            String profileImageUrl = "https://example.com/image.jpg";

            // act
            User user = User.create(kakaoId, nickname, profileImageUrl);

            // assert
            assertAll(
                () -> assertThat(user.getKakaoId()).isEqualTo(kakaoId),
                () -> assertThat(user.getNickname()).isEqualTo(nickname),
                () -> assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl)
            );
        }

        @DisplayName("kakaoId가 null이어도, 정상적으로 생성된다.")
        @Test
        void createsUser_whenKakaoIdIsNull() {
            // act
            User user = User.create(null, "홍길동", "https://example.com/image.jpg");

            // assert
            assertThat(user.getKakaoId()).isNull();
        }
    }

    @DisplayName("닉네임을 변경할 때,")
    @Nested
    class ChangeNickname {

        @DisplayName("정상적인 닉네임이 주어지면, 변경되고 trim 처리된다.")
        @Test
        void changesNickname_whenValidNicknameIsProvided() {
            // arrange
            User user = User.create(1L, "기존닉네임", "https://example.com/image.jpg");

            // act
            user.changeNickname(" 새닉네임 ");

            // assert
            assertThat(user.getNickname()).isEqualTo("새닉네임");
        }

        @DisplayName("null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNicknameIsNull() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");

            // act
            CoreException result = assertThrows(CoreException.class, () -> user.changeNickname(null));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNicknameIsEmpty() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");

            // act
            CoreException result = assertThrows(CoreException.class, () -> user.changeNickname(""));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("공백으로만 이루어져 있으면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNicknameIsBlank() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");

            // act
            CoreException result = assertThrows(CoreException.class, () -> user.changeNickname("   "));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("21자 이상이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenNicknameExceeds20Characters() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");
            String longNickname = "가".repeat(21);

            // act
            CoreException result = assertThrows(CoreException.class, () -> user.changeNickname(longNickname));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("프로필 이미지 URL을 변경할 때,")
    @Nested
    class ChangeProfileImageUrl {

        @DisplayName("정상적인 URL이 주어지면, 변경된다.")
        @Test
        void changesProfileImageUrl_whenValidUrlIsProvided() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/old.jpg");

            // act
            user.changeProfileImageUrl("https://example.com/new.jpg");

            // assert
            assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/new.jpg");
        }

        @DisplayName("null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUrlIsNull() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");

            // act
            CoreException result = assertThrows(CoreException.class, () -> user.changeProfileImageUrl(null));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenUrlIsBlank() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");

            // act
            CoreException result = assertThrows(CoreException.class, () -> user.changeProfileImageUrl(""));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }

    @DisplayName("회원 탈퇴를 할 때,")
    @Nested
    class Delete {

        @DisplayName("deletedAt이 설정된다.")
        @Test
        void setsDeletedAt_whenDeleted() {
            // arrange
            User user = User.create(1L, "홍길동", "https://example.com/image.jpg");

            // act
            user.delete();

            // assert
            assertThat(user.getDeletedAt()).isNotNull();
        }
    }
}
