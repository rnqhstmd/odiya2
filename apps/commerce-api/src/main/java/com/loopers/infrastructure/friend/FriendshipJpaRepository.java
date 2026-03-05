package com.loopers.infrastructure.friend;

import com.loopers.domain.friend.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipJpaRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT f FROM Friendship f WHERE f.id = :id AND f.deletedAt IS NULL")
    Optional<Friendship> findActiveById(@Param("id") Long id);

    @Query("SELECT f FROM Friendship f WHERE f.requester.id = :requesterId AND f.receiver.id = :receiverId AND f.deletedAt IS NULL")
    Optional<Friendship> findActiveByRequesterIdAndReceiverId(@Param("requesterId") Long requesterId, @Param("receiverId") Long receiverId);

    @Query("SELECT f FROM Friendship f WHERE ((f.requester.id = :userId1 AND f.receiver.id = :userId2) OR (f.requester.id = :userId2 AND f.receiver.id = :userId1)) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL")
    Optional<Friendship> findActiveFriendship(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL")
    List<Friendship> findAllAcceptedByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f WHERE (f.requester.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL AND ((f.requester.id = :userId AND f.requesterTag.id = :tagId) OR (f.receiver.id = :userId AND f.receiverTag.id = :tagId))")
    List<Friendship> findAllAcceptedByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Query("SELECT f FROM Friendship f WHERE f.receiver.id = :userId AND f.status = 'PENDING' AND f.deletedAt IS NULL ORDER BY f.createdAt DESC")
    List<Friendship> findAllPendingReceivedByUserId(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE ((f.requester.id = :userId1 AND f.receiver.id = :userId2) OR (f.requester.id = :userId2 AND f.receiver.id = :userId1)) AND f.isBlocked = true")
    boolean existsBlockedBetween(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f WHERE f.requester.id = :requesterId AND f.receiver.id = :receiverId AND f.deletedAt IS NULL AND (f.status = 'PENDING' OR f.status = 'ACCEPTED')")
    boolean existsPendingOrAccepted(@Param("requesterId") Long requesterId, @Param("receiverId") Long receiverId);

    @Query("SELECT f FROM Friendship f WHERE f.requesterTag.id = :tagId AND f.deletedAt IS NULL")
    List<Friendship> findAllByRequesterTagId(@Param("tagId") Long tagId);

    @Query("SELECT f FROM Friendship f WHERE f.receiverTag.id = :tagId AND f.deletedAt IS NULL")
    List<Friendship> findAllByReceiverTagId(@Param("tagId") Long tagId);

    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.requester.id = :userId OR f.receiver.id = :userId) AND f.status = 'ACCEPTED' AND f.deletedAt IS NULL AND ((f.requester.id = :userId AND f.requesterTag.id = :tagId) OR (f.receiver.id = :userId AND f.receiverTag.id = :tagId))")
    int countAcceptedByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);

    @Query("SELECT CAST(f.status AS string) FROM Friendship f WHERE ((f.requester.id = :userId1 AND f.receiver.id = :userId2) OR (f.requester.id = :userId2 AND f.receiver.id = :userId1)) AND f.deletedAt IS NULL")
    Optional<String> findFriendshipStatus(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
