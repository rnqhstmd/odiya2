package com.loopers.domain.nudge;

public interface NudgeCooldownRepository {
    boolean existsCooldown(Long appointmentId, Long senderId, Long receiverId);
    void setCooldown(Long appointmentId, Long senderId, Long receiverId);
    boolean trySetCooldown(Long appointmentId, Long senderId, Long receiverId);
}
