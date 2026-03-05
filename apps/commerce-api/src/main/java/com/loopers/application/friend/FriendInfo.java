package com.loopers.application.friend;

import com.loopers.application.tag.TagInfo;
import com.loopers.domain.friend.Friendship;
import com.loopers.domain.user.User;

public record FriendInfo(Long friendUserId, String nickname, String profileImageUrl, TagInfo tag, String status) {
    public static FriendInfo from(Friendship friendship, Long currentUserId) {
        User friend;
        com.loopers.domain.tag.Tag friendTag;
        if (friendship.isRequester(currentUserId)) {
            friend = friendship.getReceiver();
            friendTag = friendship.getRequesterTag();   // requester의 태그를 보여줌
        } else {
            friend = friendship.getRequester();
            friendTag = friendship.getReceiverTag();    // receiver의 태그를 보여줌
        }
        TagInfo tagInfo = friendTag != null ? TagInfo.from(friendTag) : null;
        return new FriendInfo(
            friend.getId(),
            friend.getNickname(),
            friend.getProfileImageUrl(),
            tagInfo,
            friendship.getStatus().name()
        );
    }
}
