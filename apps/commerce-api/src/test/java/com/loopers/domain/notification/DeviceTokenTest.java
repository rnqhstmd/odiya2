package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class DeviceTokenTest {

    private final User user = User.create(1L, "사용자", "https://example.com/user.jpg");
    private final User otherUser = User.create(2L, "다른사용자", "https://example.com/other.jpg");

    @DisplayName("디바이스 토큰을 생성할 때,")
    @Nested
    class Create {

        @DisplayName("정상적인 정보가 주어지면, active=true로 생성된다.")
        @Test
        void createsDeviceToken_withActiveTrue() {
            // act
            DeviceToken deviceToken = DeviceToken.create(user, "token-abc", DeviceType.IOS);

            // assert
            assertAll(
                () -> assertThat(deviceToken.getUser()).isEqualTo(user),
                () -> assertThat(deviceToken.getToken()).isEqualTo("token-abc"),
                () -> assertThat(deviceToken.getDeviceType()).isEqualTo(DeviceType.IOS),
                () -> assertThat(deviceToken.isActive()).isTrue()
            );
        }
    }

    @DisplayName("토큰을 비활성화할 때,")
    @Nested
    class Deactivate {

        @DisplayName("deactivate() 호출 후 active가 false가 된다.")
        @Test
        void setsActiveToFalse_afterDeactivate() {
            // arrange
            DeviceToken deviceToken = DeviceToken.create(user, "token-abc", DeviceType.ANDROID);

            // act
            deviceToken.deactivate();

            // assert
            assertThat(deviceToken.isActive()).isFalse();
        }
    }

    @DisplayName("토큰을 재활성화할 때,")
    @Nested
    class Reactivate {

        @DisplayName("reactivate() 호출 후 active=true가 되고 user가 갱신된다.")
        @Test
        void setsActiveTrueAndUpdatesUser_afterReactivate() {
            // arrange
            DeviceToken deviceToken = DeviceToken.create(user, "token-abc", DeviceType.IOS);
            deviceToken.deactivate();

            // act
            deviceToken.reactivate(otherUser);

            // assert
            assertAll(
                () -> assertThat(deviceToken.isActive()).isTrue(),
                () -> assertThat(deviceToken.getUser()).isEqualTo(otherUser)
            );
        }
    }

    @DisplayName("소유자를 확인할 때,")
    @Nested
    class IsOwnedBy {

        @DisplayName("등록한 사용자 ID이면 true를 반환한다.")
        @Test
        void returnsTrue_whenUserIsOwner() {
            // arrange
            DeviceToken deviceToken = DeviceToken.create(user, "token-abc", DeviceType.IOS);

            // assert
            assertThat(deviceToken.isOwnedBy(user.getId())).isTrue();
        }

        @DisplayName("다른 사용자 ID이면 false를 반환한다.")
        @Test
        void returnsFalse_whenUserIsNotOwner() {
            // arrange
            DeviceToken deviceToken = DeviceToken.create(user, "token-abc", DeviceType.IOS);

            // assert
            assertThat(deviceToken.isOwnedBy(999L)).isFalse();
        }
    }
}
