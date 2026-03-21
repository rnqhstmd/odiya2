package com.loopers.domain.friend;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.tag.Tag;
import com.loopers.domain.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "friendships")
public class Friendship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private FriendshipStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_tag_id")
    private Tag requesterTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_tag_id")
    private Tag receiverTag;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    protected Friendship() {}

    private Friendship(User requester, User receiver, FriendshipStatus status) {
        this.requester = requester;
        this.receiver = receiver;
        this.status = status;
        this.isBlocked = false;
    }

    public static Friendship createRequest(User requester, User receiver) {
        return new Friendship(requester, receiver, FriendshipStatus.PENDING);
    }

    public User getRequester() { return requester; }
    public User getReceiver() { return receiver; }
    public FriendshipStatus getStatus() { return status; }
    public Tag getRequesterTag() { return requesterTag; }
    public Tag getReceiverTag() { return receiverTag; }
    public boolean isBlocked() { return isBlocked; }

    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void block() {
        this.isBlocked = true;
        this.delete();
    }

    public void changeRequesterTag(Tag tag) { this.requesterTag = tag; }
    public void changeReceiverTag(Tag tag) { this.receiverTag = tag; }

    public boolean isRequester(Long userId) { return requester.getId().equals(userId); }
    public boolean isReceiver(Long userId) { return receiver.getId().equals(userId); }
}
