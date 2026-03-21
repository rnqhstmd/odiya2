package com.loopers.interfaces.api.friend;

import com.loopers.application.friend.FriendFacade;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/friends")
public class FriendV1Controller implements FriendV1ApiSpec {
    private final FriendFacade friendFacade;

    @GetMapping
    @Override
    public ApiResponse<List<FriendV1Dto.FriendResponse>> getMyFriends(
        @AuthenticationPrincipal LoginUser loginUser,
        @RequestParam(required = false) Long tagId
    ) {
        List<FriendV1Dto.FriendResponse> response = friendFacade.getMyFriends(loginUser.userId(), tagId).stream()
            .map(FriendV1Dto.FriendResponse::from).toList();
        return ApiResponse.success(response);
    }

    @PostMapping("/request")
    @Override
    public ApiResponse<Void> sendFriendRequest(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody FriendV1Dto.SendFriendRequestBody request
    ) {
        friendFacade.sendFriendRequest(loginUser.userId(), request.targetUserId());
        return ApiResponse.<Void>success();
    }

    @GetMapping("/requests/received")
    @Override
    public ApiResponse<List<FriendV1Dto.FriendRequestResponse>> getReceivedRequests(
        @AuthenticationPrincipal LoginUser loginUser
    ) {
        List<FriendV1Dto.FriendRequestResponse> response = friendFacade.getReceivedRequests(loginUser.userId()).stream()
            .map(FriendV1Dto.FriendRequestResponse::from).toList();
        return ApiResponse.success(response);
    }

    @PostMapping("/request/{requestId}/accept")
    @Override
    public ApiResponse<Void> acceptFriendRequest(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long requestId
    ) {
        friendFacade.acceptRequest(requestId, loginUser.userId());
        return ApiResponse.<Void>success();
    }

    @PostMapping("/request/{requestId}/reject")
    @Override
    public ApiResponse<Void> rejectFriendRequest(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long requestId
    ) {
        friendFacade.rejectRequest(requestId, loginUser.userId());
        return ApiResponse.<Void>success();
    }

    @DeleteMapping("/{friendUserId}")
    @Override
    public ApiResponse<Void> removeFriend(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long friendUserId
    ) {
        friendFacade.removeFriend(loginUser.userId(), friendUserId);
        return ApiResponse.<Void>success();
    }

    @PatchMapping("/{friendUserId}/tag")
    @Override
    public ApiResponse<Void> changeFriendTag(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long friendUserId,
        @Valid @RequestBody FriendV1Dto.ChangeFriendTagRequest request
    ) {
        friendFacade.changeFriendTag(loginUser.userId(), friendUserId, request.tagId());
        return ApiResponse.<Void>success();
    }
}
