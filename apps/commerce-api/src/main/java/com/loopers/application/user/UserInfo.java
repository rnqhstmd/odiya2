package com.loopers.application.user;

import com.loopers.domain.user.User;

public record UserInfo(Long id, String nickname, String profileImageUrl) {
    public static UserInfo from(User model) {
        return new UserInfo(
            model.getId(),
            model.getNickname(),
            model.getProfileImageUrl()
        );
    }
}
