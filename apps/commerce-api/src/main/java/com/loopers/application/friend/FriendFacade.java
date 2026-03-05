package com.loopers.application.friend;

import com.loopers.domain.friend.FriendService;
import com.loopers.domain.friend.Friendship;
import com.loopers.domain.tag.Tag;
import com.loopers.domain.tag.TagService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class FriendFacade {
    private final FriendService friendService;
    private final UserService userService;
    private final TagService tagService;

    public List<FriendInfo> getMyFriends(Long userId, Long tagId) {
        if (tagId != null) {
            Tag tag = tagService.getTag(tagId);
            if (!tag.getOwner().getId().equals(userId)) {
                throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 태그입니다.");
            }
        }
        List<Friendship> friendships = friendService.getAcceptedFriends(userId, tagId);
        return friendships.stream()
            .map(f -> FriendInfo.from(f, userId))
            .toList();
    }

    public void sendFriendRequest(Long requesterId, Long targetUserId) {
        User requester = userService.getUser(requesterId);
        User receiver = userService.getUser(targetUserId);
        friendService.sendRequest(requester, receiver);
    }

    public List<FriendRequestInfo> getReceivedRequests(Long userId) {
        return friendService.getPendingReceivedRequests(userId).stream()
            .map(FriendRequestInfo::from)
            .toList();
    }

    public void acceptRequest(Long requestId, Long userId) {
        friendService.acceptRequest(requestId, userId);
        // 수락 시 양쪽 모두 기본 태그 할당
        Friendship friendship = friendService.getFriendship(requestId);
        Tag requesterDefaultTag = tagService.getDefaultTag(friendship.getRequester().getId());
        Tag receiverDefaultTag = tagService.getDefaultTag(friendship.getReceiver().getId());
        friendship.changeRequesterTag(requesterDefaultTag);
        friendship.changeReceiverTag(receiverDefaultTag);
    }

    public void rejectRequest(Long requestId, Long userId) {
        friendService.rejectRequest(requestId, userId);
    }

    public void removeFriend(Long userId, Long friendUserId) {
        friendService.removeFriend(userId, friendUserId);
    }

    public void changeFriendTag(Long userId, Long friendUserId, Long tagId) {
        Tag tag = tagService.getTag(tagId);
        if (!tag.getOwner().getId().equals(userId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 태그입니다.");
        }
        friendService.changeFriendTag(userId, friendUserId, tag);
    }
}
