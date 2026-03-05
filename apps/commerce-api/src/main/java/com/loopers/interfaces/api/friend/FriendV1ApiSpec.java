package com.loopers.interfaces.api.friend;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Friend V1 API", description = "친구 관리 API")
public interface FriendV1ApiSpec {
    @Operation(summary = "내 친구 목록", description = "로그인한 사용자의 친구 목록을 조회합니다.")
    ApiResponse<List<FriendV1Dto.FriendResponse>> getMyFriends(LoginUser loginUser, Long tagId);

    @Operation(summary = "친구 요청 보내기", description = "대상 사용자에게 친구 요청을 보냅니다.")
    ApiResponse<Void> sendFriendRequest(LoginUser loginUser, FriendV1Dto.SendFriendRequestBody request);

    @Operation(summary = "받은 친구 요청 목록", description = "받은 친구 요청 목록을 조회합니다.")
    ApiResponse<List<FriendV1Dto.FriendRequestResponse>> getReceivedRequests(LoginUser loginUser);

    @Operation(summary = "친구 요청 수락", description = "친구 요청을 수락합니다.")
    ApiResponse<Void> acceptFriendRequest(LoginUser loginUser, Long requestId);

    @Operation(summary = "친구 요청 거절", description = "친구 요청을 거절합니다.")
    ApiResponse<Void> rejectFriendRequest(LoginUser loginUser, Long requestId);

    @Operation(summary = "친구 끊기", description = "친구 관계를 끊습니다. (= 차단)")
    ApiResponse<Void> removeFriend(LoginUser loginUser, Long friendUserId);

    @Operation(summary = "친구 태그 변경", description = "친구에게 부여된 태그를 변경합니다.")
    ApiResponse<Void> changeFriendTag(LoginUser loginUser, Long friendUserId, FriendV1Dto.ChangeFriendTagRequest request);
}
