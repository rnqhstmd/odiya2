package com.loopers.infrastructure.friend;

import com.loopers.domain.friend.Friendship;
import com.loopers.domain.friend.FriendshipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FriendshipRepositoryImpl implements FriendshipRepository {
    private final FriendshipJpaRepository friendshipJpaRepository;

    @Override public Friendship save(Friendship friendship) { return friendshipJpaRepository.save(friendship); }
    @Override public Optional<Friendship> findActiveById(Long id) { return friendshipJpaRepository.findActiveById(id); }
    @Override public Optional<Friendship> findActiveByRequesterIdAndReceiverId(Long requesterId, Long receiverId) { return friendshipJpaRepository.findActiveByRequesterIdAndReceiverId(requesterId, receiverId); }
    @Override public Optional<Friendship> findActiveFriendship(Long userId1, Long userId2) { return friendshipJpaRepository.findActiveFriendship(userId1, userId2); }
    @Override public List<Friendship> findAllAcceptedByUserId(Long userId) { return friendshipJpaRepository.findAllAcceptedByUserId(userId); }
    @Override public List<Friendship> findAllAcceptedByUserIdAndTagId(Long userId, Long tagId) { return friendshipJpaRepository.findAllAcceptedByUserIdAndTagId(userId, tagId); }
    @Override public List<Friendship> findAllPendingReceivedByUserId(Long userId) { return friendshipJpaRepository.findAllPendingReceivedByUserId(userId); }
    @Override public boolean existsBlockedBetween(Long userId1, Long userId2) { return friendshipJpaRepository.existsBlockedBetween(userId1, userId2); }
    @Override public boolean existsPendingOrAccepted(Long requesterId, Long receiverId) { return friendshipJpaRepository.existsPendingOrAccepted(requesterId, receiverId); }
    @Override public List<Friendship> findAllByRequesterTagId(Long tagId) { return friendshipJpaRepository.findAllByRequesterTagId(tagId); }
    @Override public List<Friendship> findAllByReceiverTagId(Long tagId) { return friendshipJpaRepository.findAllByReceiverTagId(tagId); }
    @Override public int countAcceptedByUserIdAndTagId(Long userId, Long tagId) { return friendshipJpaRepository.countAcceptedByUserIdAndTagId(userId, tagId); }
    @Override public String findFriendshipStatus(Long userId1, Long userId2) { return friendshipJpaRepository.findFriendshipStatus(userId1, userId2).map(Enum::name).orElse(null); }
}
