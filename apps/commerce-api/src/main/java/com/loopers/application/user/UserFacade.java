package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;

    public UserInfo getUser(Long userId) {
        UserModel user = userService.getUser(userId);
        return UserInfo.from(user);
    }

    public UserInfo updateNickname(Long userId, String nickname) {
        UserModel user = userService.updateNickname(userId, nickname);
        return UserInfo.from(user);
    }

    public UserInfo updateProfileImage(Long userId, String url) {
        UserModel user = userService.updateProfileImage(userId, url);
        return UserInfo.from(user);
    }

    public void withdraw(Long userId) {
        userService.withdraw(userId);
    }
}
