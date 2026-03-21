package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeviceTokenServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private DeviceTokenService deviceTokenService;

    private final User user = User.create(1L, "사용자", "https://example.com/user.jpg");
    private final User otherUser = User.create(2L, "다른사용자", "https://example.com/other.jpg");

    @DisplayName("register를 호출할 때,")
    @Nested
    class Register {

        @DisplayName("토큰이 신규이면 새로 생성하여 반환한다.")
        @Test
        void createsNewToken_whenTokenDoesNotExist() {
            // arrange
            DeviceToken newToken = DeviceToken.create(user, "new-token", DeviceType.IOS);
            given(deviceTokenRepository.findByToken("new-token")).willReturn(Optional.empty());
            given(deviceTokenRepository.save(any())).willReturn(newToken);

            // act
            DeviceToken result = deviceTokenService.register(user, "new-token", DeviceType.IOS);

            // assert
            assertThat(result).isEqualTo(newToken);
            verify(deviceTokenRepository).save(any());
        }

        @DisplayName("이미 존재하는 토큰이면 reactivate 후 저장하여 반환한다.")
        @Test
        void reactivatesExistingToken_whenTokenAlreadyExists() {
            // arrange
            DeviceToken existing = DeviceToken.create(otherUser, "existing-token", DeviceType.ANDROID);
            existing.deactivate();
            given(deviceTokenRepository.findByToken("existing-token")).willReturn(Optional.of(existing));
            given(deviceTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            DeviceToken result = deviceTokenService.register(user, "existing-token", DeviceType.ANDROID);

            // assert
            assertThat(result.isActive()).isTrue();
            assertThat(result.getUser()).isEqualTo(user);
            verify(deviceTokenRepository).save(existing);
        }
    }

    @DisplayName("deactivate를 호출할 때,")
    @Nested
    class Deactivate {

        @DisplayName("본인의 토큰이면 비활성화된다.")
        @Test
        void deactivatesToken_whenTokenBelongsToUser() {
            // arrange
            DeviceToken deviceToken = DeviceToken.create(user, "my-token", DeviceType.IOS);
            given(deviceTokenRepository.findByToken("my-token")).willReturn(Optional.of(deviceToken));
            given(deviceTokenRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            // act
            deviceTokenService.deactivate("my-token", user.getId());

            // assert
            assertThat(deviceToken.isActive()).isFalse();
            verify(deviceTokenRepository).save(deviceToken);
        }

        @DisplayName("타인의 토큰이면 비활성화하지 않는다.")
        @Test
        void doesNotDeactivate_whenTokenBelongsToOtherUser() {
            // arrange: 모든 비영속 엔티티의 getId()는 0L이므로, 다른 userId(999L)로 타인을 표현한다.
            DeviceToken deviceToken = DeviceToken.create(user, "other-token", DeviceType.IOS);
            given(deviceTokenRepository.findByToken("other-token")).willReturn(Optional.of(deviceToken));

            // act: 999L은 user.getId()(=0L)와 다르므로 소유자 불일치
            deviceTokenService.deactivate("other-token", 999L);

            // assert
            assertThat(deviceToken.isActive()).isTrue();
            verify(deviceTokenRepository, org.mockito.Mockito.never()).save(any());
        }
    }

    @DisplayName("getActiveTokenStrings를 호출할 때,")
    @Nested
    class GetActiveTokenStrings {

        @DisplayName("활성 토큰의 문자열 목록을 반환한다.")
        @Test
        void returnsActiveTokenStrings_forUser() {
            // arrange
            DeviceToken token1 = DeviceToken.create(user, "token-aaa", DeviceType.IOS);
            DeviceToken token2 = DeviceToken.create(user, "token-bbb", DeviceType.ANDROID);
            given(deviceTokenRepository.findActiveByUserId(user.getId())).willReturn(List.of(token1, token2));

            // act
            List<String> result = deviceTokenService.getActiveTokenStrings(user.getId());

            // assert
            assertThat(result).containsExactly("token-aaa", "token-bbb");
        }
    }
}
