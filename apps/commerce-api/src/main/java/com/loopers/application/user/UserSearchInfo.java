package com.loopers.application.user;

import com.loopers.domain.user.User;

public record UserSearchInfo(Long id, String nickname, String profileImageUrl, String friendStatus) {
    public static UserSearchInfo of(User user, String friendStatus) {
        return new UserSearchInfo(user.getId(), user.getNickname(), user.getProfileImageUrl(), friendStatus);
    }
}
