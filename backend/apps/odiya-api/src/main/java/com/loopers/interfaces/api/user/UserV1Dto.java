package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;
import com.loopers.application.user.UserSearchInfo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserV1Dto {

    public record UpdateNicknameRequest(@NotBlank @Size(min = 2, max = 20) String nickname) {}

    public record UpdateProfileImageRequest(@NotBlank @Pattern(regexp = "^https?://.*$") String profileImageUrl) {}

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
