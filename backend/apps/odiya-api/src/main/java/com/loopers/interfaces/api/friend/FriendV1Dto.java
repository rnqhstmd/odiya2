package com.loopers.interfaces.api.friend;

import com.loopers.application.friend.FriendInfo;
import com.loopers.application.friend.FriendRequestInfo;
import com.loopers.interfaces.api.tag.TagV1Dto;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public class FriendV1Dto {
    public record SendFriendRequestBody(@NotNull Long targetUserId) {}
    public record ChangeFriendTagRequest(@NotNull Long tagId) {}

    public record FriendResponse(Long friendUserId, String nickname, String profileImageUrl, TagV1Dto.TagResponse tag, String status) {
        public static FriendResponse from(FriendInfo info) {
            TagV1Dto.TagResponse tagResponse = info.tag() != null
                ? new TagV1Dto.TagResponse(info.tag().id(), info.tag().name(), info.tag().color(), info.tag().isDefault(), info.tag().friendCount())
                : null;
            return new FriendResponse(info.friendUserId(), info.nickname(), info.profileImageUrl(), tagResponse, info.status());
        }
    }

    public record FriendRequestResponse(Long requestId, Long fromUserId, String nickname, String profileImageUrl, ZonedDateTime createdAt) {
        public static FriendRequestResponse from(FriendRequestInfo info) {
            return new FriendRequestResponse(info.requestId(), info.fromUserId(), info.nickname(), info.profileImageUrl(), info.createdAt());
        }
    }
}
