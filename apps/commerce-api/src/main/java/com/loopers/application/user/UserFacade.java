package com.loopers.application.user;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {

    private final UserService userService;

    public UserInfo getUser(Long userId) {
        User user = userService.getUser(userId);
        return UserInfo.from(user);
    }

    public UserInfo updateNickname(Long userId, String nickname) {
        User user = userService.updateNickname(userId, nickname);
        return UserInfo.from(user);
    }

    public UserInfo updateProfileImage(Long userId, String url) {
        User user = userService.updateProfileImage(userId, url);
        return UserInfo.from(user);
    }

    public void withdraw(Long userId) {
        userService.withdraw(userId);
    }
}
