package com.loopers.infrastructure.place;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.place.PlaceSearchResult;
import com.loopers.domain.place.PlaceCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class PlaceCacheRepositoryImpl implements PlaceCacheRepository {

    private static final String KEY_PREFIX = "place:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, String> replicaRedisTemplate;
    private final RedisTemplate<String, String> masterRedisTemplate;
    private final ObjectMapper objectMapper;

    public PlaceCacheRepositoryImpl(
        RedisTemplate<String, String> replicaRedisTemplate,
        @Qualifier("redisTemplateMaster") RedisTemplate<String, String> masterRedisTemplate,
        ObjectMapper objectMapper
    ) {
        this.replicaRedisTemplate = replicaRedisTemplate;
        this.masterRedisTemplate = masterRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<PlaceSearchResult> find(String keyword, int page, int size) {
        try {
            String key = buildKey(keyword, page, size);
            String json = replicaRedisTemplate.opsForValue().get(key);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, PlaceSearchResult.class));
        } catch (Exception e) {
            log.warn("Redis 캐시 조회 실패 (장소 검색): error={}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void save(String keyword, int page, int size, PlaceSearchResult result) {
        try {
            String key = buildKey(keyword, page, size);
            String json = objectMapper.writeValueAsString(result);
            masterRedisTemplate.opsForValue().set(key, json, TTL);
        } catch (JsonProcessingException e) {
            log.warn("Redis 캐시 직렬화 실패 (장소 검색): error={}", e.getMessage(), e);
        } catch (Exception e) {
            log.warn("Redis 캐시 저장 실패 (장소 검색): error={}", e.getMessage(), e);
        }
    }

    private String buildKey(String keyword, int page, int size) {
        return KEY_PREFIX + hashKeyword(keyword) + ":" + page + ":" + size;
    }

    private String hashKeyword(String keyword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(keyword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
