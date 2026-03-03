package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import jakarta.validation.constraints.NotBlank;

public class UserV1Dto {

    public record UpdateNicknameRequest(@NotBlank String nickname) {}

    public record UpdateProfileImageRequest(@NotBlank String profileImageUrl) {}

    public record UserResponse(Long id, String nickname, String profileImageUrl) {
        public static UserResponse from(UserInfo info) {
            return new UserResponse(info.id(), info.nickname(), info.profileImageUrl());
        }
    }
}
