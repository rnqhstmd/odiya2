package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {

    private final UserFacade userFacade;

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> getMe(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        UserInfo userInfo = userFacade.getUser(loginUser.userId());
        return ApiResponse.success(UserV1Dto.UserResponse.from(userInfo));
    }

    @PatchMapping("/me/nickname")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> updateNickname(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody UserV1Dto.UpdateNicknameRequest request
    ) {
        UserInfo userInfo = userFacade.updateNickname(loginUser.userId(), request.nickname());
        return ApiResponse.success(UserV1Dto.UserResponse.from(userInfo));
    }

    @PatchMapping("/me/profile-image")
    @Override
    public ApiResponse<UserV1Dto.UserResponse> updateProfileImage(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody UserV1Dto.UpdateProfileImageRequest request
    ) {
        UserInfo userInfo = userFacade.updateProfileImage(loginUser.userId(), request.profileImageUrl());
        return ApiResponse.success(UserV1Dto.UserResponse.from(userInfo));
    }

    @DeleteMapping("/me")
    @Override
    public ApiResponse<Void> withdraw(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        userFacade.withdraw(loginUser.userId());
        return ApiResponse.<Void>success();
    }
}
