package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("사용자를 찾거나 생성할 때,")
    @Nested
    class FindOrCreateUser {

        @DisplayName("신규 사용자이면, 새로 생성하여 반환한다.")
        @Test
        void createsNewUser_whenUserDoesNotExist() {
            // act
            User user = userService.findOrCreateUser(12345L, "홍길동", "https://example.com/image.jpg");

            // assert
            assertAll(
                () -> assertThat(user.getId()).isNotNull(),
                () -> assertThat(user.getKakaoId()).isEqualTo(12345L),
                () -> assertThat(user.getNickname()).isEqualTo("홍길동"),
                () -> assertThat(user.getProfileImageUrl()).isEqualTo("https://example.com/image.jpg")
            );
        }

        @DisplayName("기존 사용자이면, 기존 사용자를 반환한다.")
        @Test
        void returnsExistingUser_whenUserAlreadyExists() {
            // arrange
            User existing = userService.findOrCreateUser(12345L, "홍길동", "https://example.com/image.jpg");

            // act
            User result = userService.findOrCreateUser(12345L, "다른닉네임", "https://example.com/other.jpg");

            // assert
            assertThat(result.getId()).isEqualTo(existing.getId());
        }

        @DisplayName("프로필 이미지가 null이면, 기본 이미지를 사용한다.")
        @Test
        void usesDefaultImage_whenProfileImageUrlIsNull() {
            // act
            User user = userService.findOrCreateUser(12345L, "홍길동", null);

            // assert
            assertThat(user.getProfileImageUrl()).isEqualTo("https://cdn.odiya.com/images/default-profile.png");
        }

        @DisplayName("탈퇴한 사용자와 같은 kakaoId이면, 새로 생성한다.")
        @Test
        void createsNewUser_whenPreviousUserIsWithdrawn() {
            // arrange
            User withdrawn = userService.findOrCreateUser(12345L, "홍길동", "https://example.com/image.jpg");
            userService.withdraw(withdrawn.getId());

            // act
            User newUser = userService.findOrCreateUser(12345L, "홍길동", "https://example.com/image.jpg");

            // assert
            assertThat(newUser.getId()).isNotEqualTo(withdrawn.getId());
        }
    }

    @DisplayName("사용자를 조회할 때,")
    @Nested
    class GetUser {

        @DisplayName("존재하는 사용자 ID를 주면, 해당 사용자를 반환한다.")
        @Test
        void returnsUser_whenValidIdIsProvided() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));

            // act
            User result = userService.getUser(saved.getId());

            // assert
            assertThat(result.getId()).isEqualTo(saved.getId());
        }

        @DisplayName("존재하지 않는 ID를 주면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenInvalidIdIsProvided() {
            // act
            CoreException result = assertThrows(CoreException.class, () -> userService.getUser(999L));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

        @DisplayName("탈퇴한 사용자 ID를 주면, NOT_FOUND 예외가 발생한다.")
        @Test
        void throwsNotFound_whenUserIsWithdrawn() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));
            userService.withdraw(saved.getId());

            // act
            CoreException result = assertThrows(CoreException.class, () -> userService.getUser(saved.getId()));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }

    @DisplayName("닉네임을 변경할 때,")
    @Nested
    class UpdateNickname {

        @DisplayName("정상적인 닉네임이면, 변경된다.")
        @Test
        void updatesNickname_whenValidNicknameIsProvided() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));

            // act
            User result = userService.updateNickname(saved.getId(), "새닉네임");

            // assert
            assertThat(result.getNickname()).isEqualTo("새닉네임");
        }

        @DisplayName("이미 사용 중인 닉네임이면, CONFLICT 예외가 발생한다.")
        @Test
        void throwsConflict_whenNicknameIsDuplicate() {
            // arrange
            userJpaRepository.save(User.create(11111L, "기존닉네임", "https://example.com/a.jpg"));
            User target = userJpaRepository.save(User.create(22222L, "다른닉네임", "https://example.com/b.jpg"));

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> userService.updateNickname(target.getId(), "기존닉네임"));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.CONFLICT);
        }

        @DisplayName("자기 자신의 닉네임으로 변경하면, 정상적으로 처리된다.")
        @Test
        void updatesNickname_whenSameAsCurrentNickname() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));

            // act
            User result = userService.updateNickname(saved.getId(), "홍길동");

            // assert
            assertThat(result.getNickname()).isEqualTo("홍길동");
        }
    }

    @DisplayName("회원 탈퇴를 할 때,")
    @Nested
    class Withdraw {

        @DisplayName("soft delete가 수행된다.")
        @Test
        void softDeletesUser_whenWithdrawIsCalled() {
            // arrange
            User saved = userJpaRepository.save(User.create(12345L, "홍길동", "https://example.com/image.jpg"));

            // act
            userService.withdraw(saved.getId());

            // assert
            CoreException result = assertThrows(CoreException.class, () -> userService.getUser(saved.getId()));
            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
