package com.loopers.domain.friend;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository {
    Friendship save(Friendship friendship);
    Optional<Friendship> findActiveById(Long id);
    Optional<Friendship> findActiveByRequesterIdAndReceiverId(Long requesterId, Long receiverId);
    Optional<Friendship> findActiveFriendship(Long userId1, Long userId2);
    List<Friendship> findAllAcceptedByUserId(Long userId);
    List<Friendship> findAllAcceptedByUserIdAndTagId(Long userId, Long tagId);
    List<Friendship> findAllPendingReceivedByUserId(Long userId);
    boolean existsBlockedBetween(Long userId1, Long userId2);
    boolean existsPendingOrAccepted(Long requesterId, Long receiverId);
    List<Friendship> findAllByRequesterTagId(Long tagId);
    List<Friendship> findAllByReceiverTagId(Long tagId);
    int countAcceptedByUserIdAndTagId(Long userId, Long tagId);
    String findFriendshipStatus(Long userId1, Long userId2);
    boolean existsAcceptedFriendship(Long userId1, Long userId2);
}
