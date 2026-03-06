package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserSearchInfo;
import jakarta.validation.constraints.NotBlank;

public class UserV1Dto {

    public record UpdateNicknameRequest(@NotBlank String nickname) {}

    public record UpdateProfileImageRequest(@NotBlank String profileImageUrl) {}

    public record UserResponse(Long id, String nickname, String profileImageUrl) {
        public static UserResponse from(UserInfo info) {
            return new UserResponse(info.id(), info.nickname(), info.profileImageUrl());
        }
    }

    public record UserSearchResponse(Long id, String nickname, String profileImageUrl, String friendStatus) {
        public static UserSearchResponse from(UserSearchInfo info) {
            return new UserSearchResponse(info.id(), info.nickname(), info.profileImageUrl(), info.friendStatus());
        }
    }
}
