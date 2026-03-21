package com.loopers.application.friend;

import com.loopers.domain.friend.Friendship;

import java.time.ZonedDateTime;

public record FriendRequestInfo(Long requestId, Long fromUserId, String nickname, String profileImageUrl, ZonedDateTime createdAt) {
    public static FriendRequestInfo from(Friendship friendship) {
        return new FriendRequestInfo(
            friendship.getId(),
            friendship.getRequester().getId(),
            friendship.getRequester().getNickname(),
            friendship.getRequester().getProfileImageUrl(),
            friendship.getCreatedAt()
        );
    }
}
