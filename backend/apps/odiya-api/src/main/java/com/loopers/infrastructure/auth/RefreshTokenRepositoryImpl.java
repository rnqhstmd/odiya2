package com.loopers.infrastructure.auth;

import com.loopers.domain.auth.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "refresh:";
    private static final Duration TTL = Duration.ofDays(30);

    private final RedisTemplate<String, String> redisTemplate;

    public RefreshTokenRepositoryImpl(
        @Qualifier("redisTemplateMaster") RedisTemplate<String, String> redisTemplate
    ) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void save(Long userId, String tokenId, String refreshToken) {
        String key = buildKey(userId, tokenId);
        redisTemplate.opsForValue().set(key, refreshToken, TTL);
    }

    @Override
    public Optional<String> find(Long userId, String tokenId) {
        String key = buildKey(userId, tokenId);
        String value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    @Override
    public boolean deleteIfPresent(Long userId, String tokenId) {
        String key = buildKey(userId, tokenId);
        Boolean deleted = redisTemplate.delete(key);
        return Boolean.TRUE.equals(deleted);
    }

    private String buildKey(Long userId, String tokenId) {
        return KEY_PREFIX + userId + ":" + tokenId;
    }
}
