package com.loopers.application.friend;

import com.loopers.application.common.ErrorMessages;
import com.loopers.application.notification.NotificationFacade;
import com.loopers.domain.friend.FriendService;
import com.loopers.domain.friend.Friendship;
import com.loopers.domain.notification.NotificationType;
import com.loopers.domain.tag.Tag;
import com.loopers.domain.tag.TagService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FriendFacade {
    private final FriendService friendService;
    private final UserService userService;
    private final TagService tagService;
    private final NotificationFacade notificationFacade;

    @Transactional(readOnly = true)
    public List<FriendInfo> getMyFriends(Long userId, Long tagId) {
        if (tagId != null) {
            Tag tag = tagService.getTag(tagId);
            if (!tag.getOwner().getId().equals(userId)) {
                throw ErrorMessages.TAG_NOT_FOUND.asException();
            }
        }
        List<Friendship> friendships = friendService.getAcceptedFriends(userId, tagId);
        return friendships.stream()
            .map(f -> FriendInfo.from(f, userId))
            .toList();
    }

    @Transactional
    public void sendFriendRequest(Long requesterId, Long targetUserId) {
        User requester = userService.getUser(requesterId);
        User receiver = userService.getUser(targetUserId);
        friendService.sendRequest(requester, receiver);

        try {
            notificationFacade.sendNotification(
                receiver, requester, NotificationType.FRIEND_REQUEST,
                requester.getNickname() + "님이 친구 요청을 보냈어요",
                "수락하여 친구가 되어보세요.",
                null, null);
        } catch (Exception e) {
            log.warn("친구 요청 알림 발송 실패: requesterId={}, targetUserId={}, error={}",
                requesterId, targetUserId, e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<FriendRequestInfo> getReceivedRequests(Long userId) {
        return friendService.getPendingReceivedRequests(userId).stream()
            .map(FriendRequestInfo::from)
            .toList();
    }

    @Transactional
    public void acceptRequest(Long requestId, Long userId) {
        friendService.acceptRequest(requestId, userId);
        // 수락 시 양쪽 모두 기본 태그 할당
        Friendship friendship = friendService.getFriendship(requestId);
        Tag requesterDefaultTag = tagService.getDefaultTag(friendship.getRequester().getId());
        Tag receiverDefaultTag = tagService.getDefaultTag(friendship.getReceiver().getId());
        friendship.changeRequesterTag(requesterDefaultTag);
        friendship.changeReceiverTag(receiverDefaultTag);

        try {
            User requester = friendship.getRequester();
            User accepter = friendship.getReceiver();
            notificationFacade.sendNotification(
                requester, accepter, NotificationType.FRIEND_ACCEPTED,
                "친구 요청 수락",
                accepter.getNickname() + "님이 친구 요청을 수락했어요.",
                null, "FRIEND_REQUEST");
        } catch (Exception e) {
            log.warn("친구 수락 알림 발송 실패: requestId={}, error={}", requestId, e.getMessage());
        }
    }

    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        friendService.rejectRequest(requestId, userId);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendUserId) {
        friendService.removeFriend(userId, friendUserId);
    }

    @Transactional
    public void changeFriendTag(Long userId, Long friendUserId, Long tagId) {
        Tag tag = tagService.getTag(tagId);
        if (!tag.getOwner().getId().equals(userId)) {
            throw ErrorMessages.TAG_NOT_FOUND.asException();
        }
        friendService.changeFriendTag(userId, friendUserId, tag);
    }
}
