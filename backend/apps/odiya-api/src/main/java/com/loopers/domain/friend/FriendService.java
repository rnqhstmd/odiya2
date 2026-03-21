package com.loopers.domain.friend;

import com.loopers.domain.tag.Tag;
import com.loopers.domain.user.User;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class FriendService {
    private final FriendshipRepository friendshipRepository;

    public FriendService(FriendshipRepository friendshipRepository) {
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional
    public Friendship sendRequest(User requester, User receiver) {
        if (requester.getId().equals(receiver.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }
        if (friendshipRepository.existsBlockedBetween(requester.getId(), receiver.getId())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "친구 요청을 보낼 수 없는 사용자입니다.");
        }
        if (friendshipRepository.existsPendingOrAccepted(requester.getId(), receiver.getId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 친구이거나 요청이 진행 중입니다.");
        }
        if (friendshipRepository.existsPendingOrAccepted(receiver.getId(), requester.getId())) {
            throw new CoreException(ErrorType.CONFLICT, "이미 친구이거나 요청이 진행 중입니다.");
        }
        Friendship friendship = Friendship.createRequest(requester, receiver);
        return friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptRequest(Long requestId, Long receiverId) {
        Friendship friendship = getFriendship(requestId);
        if (!friendship.isReceiver(receiverId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 친구 요청입니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 중인 요청만 수락할 수 있습니다.");
        }
        friendship.accept();
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void rejectRequest(Long requestId, Long receiverId) {
        Friendship friendship = getFriendship(requestId);
        if (!friendship.isReceiver(receiverId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 친구 요청입니다.");
        }
        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new CoreException(ErrorType.BAD_REQUEST, "대기 중인 요청만 거절할 수 있습니다.");
        }
        friendship.delete();
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendUserId) {
        Friendship friendship = friendshipRepository.findActiveFriendship(userId, friendUserId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "친구 관계를 찾을 수 없습니다."));
        friendship.block();
        friendshipRepository.save(friendship);
    }

    @Transactional(readOnly = true)
    public List<Friendship> getAcceptedFriends(Long userId, Long tagId) {
        if (tagId != null) {
            return friendshipRepository.findAllAcceptedByUserIdAndTagId(userId, tagId);
        }
        return friendshipRepository.findAllAcceptedByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Friendship> getPendingReceivedRequests(Long userId) {
        return friendshipRepository.findAllPendingReceivedByUserId(userId);
    }

    @Transactional
    public void changeFriendTag(Long userId, Long friendUserId, Tag tag) {
        Friendship friendship = friendshipRepository.findActiveFriendship(userId, friendUserId)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "친구 관계를 찾을 수 없습니다."));
        if (friendship.isRequester(userId)) {
            friendship.changeRequesterTag(tag);   // requester가 자기 태그를 변경
        } else {
            friendship.changeReceiverTag(tag);    // receiver가 자기 태그를 변경
        }
        friendshipRepository.save(friendship);
    }

    public Friendship getFriendship(Long id) {
        return friendshipRepository.findActiveById(id)
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 친구 요청입니다."));
    }

    @Transactional(readOnly = true)
    public boolean isAcceptedFriend(Long userId1, Long userId2) {
        return friendshipRepository.existsAcceptedFriendship(userId1, userId2);
    }

    @Transactional(readOnly = true)
    public int countAcceptedFriendsByTag(Long userId, Long tagId) {
        return friendshipRepository.countAcceptedByUserIdAndTagId(userId, tagId);
    }

    @Transactional
    public void revertFriendshipsToDefaultTag(Long tagId, Tag defaultTag) {
        List<Friendship> byRequesterTag = friendshipRepository.findAllByRequesterTagId(tagId);
        for (Friendship f : byRequesterTag) {
            f.changeRequesterTag(defaultTag);
        }
        List<Friendship> byReceiverTag = friendshipRepository.findAllByReceiverTagId(tagId);
        for (Friendship f : byReceiverTag) {
            f.changeReceiverTag(defaultTag);
        }
    }
}
