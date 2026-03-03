package com.loopers.application.user;

import com.loopers.domain.user.UserModel;

public record UserInfo(Long id, String nickname, String profileImageUrl) {
    public static UserInfo from(UserModel model) {
        return new UserInfo(
            model.getId(),
            model.getNickname(),
            model.getProfileImageUrl()
        );
    }
}
