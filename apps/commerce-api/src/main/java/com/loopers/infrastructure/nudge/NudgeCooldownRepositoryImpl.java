package com.loopers.infrastructure.nudge;

import com.loopers.domain.nudge.NudgeCooldownRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class NudgeCooldownRepositoryImpl implements NudgeCooldownRepository {

    private static final String KEY_PREFIX = "nudge:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    public NudgeCooldownRepositoryImpl(
        @Qualifier("redisTemplateMaster") RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean existsCooldown(Long appointmentId, Long senderId, Long receiverId) {
        String key = buildKey(appointmentId, senderId, receiverId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void setCooldown(Long appointmentId, Long senderId, Long receiverId) {
        String key = buildKey(appointmentId, senderId, receiverId);
        redisTemplate.opsForValue().set(key, "1", TTL);
    }

    @Override
    public boolean trySetCooldown(Long appointmentId, Long senderId, Long receiverId) {
        String key = buildKey(appointmentId, senderId, receiverId);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", TTL));
    }

    private String buildKey(Long appointmentId, Long senderId, Long receiverId) {
        return KEY_PREFIX + appointmentId + ":" + senderId + ":" + receiverId;
    }
}
