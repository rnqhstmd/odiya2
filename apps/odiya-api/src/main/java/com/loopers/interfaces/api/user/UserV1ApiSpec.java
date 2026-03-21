package com.loopers.interfaces.api.user;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "User V1 API", description = "회원 프로필 조회 및 관리 API")
public interface UserV1ApiSpec {

    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 사용자의 프로필 정보를 조회합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> getMe(LoginUser loginUser);

    @Operation(
        summary = "닉네임 변경",
        description = "로그인한 사용자의 닉네임을 변경합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> updateNickname(LoginUser loginUser, UserV1Dto.UpdateNicknameRequest request);

    @Operation(
        summary = "프로필 이미지 변경",
        description = "로그인한 사용자의 프로필 이미지 URL을 변경합니다."
    )
    ApiResponse<UserV1Dto.UserResponse> updateProfileImage(LoginUser loginUser, UserV1Dto.UpdateProfileImageRequest request);

    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 사용자의 계정을 탈퇴 처리합니다."
    )
    ApiResponse<Void> withdraw(LoginUser loginUser);

    @Operation(
        summary = "사용자 검색",
        description = "닉네임으로 사용자를 검색합니다. 2자 이상 입력 필요."
    )
    ApiResponse<List<UserV1Dto.UserSearchResponse>> searchUsers(LoginUser loginUser, String nickname);
}
