package com.loopers.infrastructure.traveltime;

import com.loopers.domain.traveltime.CoordinateHash;
import com.loopers.domain.traveltime.TravelTimeCacheRepository;
import com.loopers.domain.usersettings.TransportType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class TravelTimeCacheRepositoryImpl implements TravelTimeCacheRepository {

    private static final String KEY_PREFIX = "travel:";

    private final RedisTemplate<String, String> replicaRedisTemplate;
    private final RedisTemplate<String, String> masterRedisTemplate;

    public TravelTimeCacheRepositoryImpl(
        RedisTemplate<String, String> replicaRedisTemplate,
        @Qualifier("redisTemplateMaster") RedisTemplate<String, String> masterRedisTemplate
    ) {
        this.replicaRedisTemplate = replicaRedisTemplate;
        this.masterRedisTemplate = masterRedisTemplate;
    }

    @Override
    public Optional<Integer> find(double originLat, double originLng, double destLat, double destLng,
                                  TransportType transportType) {
        try {
            String key = buildKey(originLat, originLng, destLat, destLng, transportType);
            String value = replicaRedisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(Integer.parseInt(value));
        } catch (Exception e) {
            log.warn("Redis 캐시 조회 실패 (이동시간): error={}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void save(double originLat, double originLng, double destLat, double destLng,
                     TransportType transportType, int durationMinutes) {
        try {
            String key = buildKey(originLat, originLng, destLat, destLng, transportType);
            Duration ttl = resolveTtl(transportType);
            if (ttl == null) {
                masterRedisTemplate.opsForValue().set(key, String.valueOf(durationMinutes));
            } else {
                masterRedisTemplate.opsForValue().set(key, String.valueOf(durationMinutes), ttl);
            }
        } catch (Exception e) {
            log.warn("Redis 캐시 저장 실패 (이동시간): error={}", e.getMessage(), e);
        }
    }

    @Override
    public void evict(double originLat, double originLng, double destLat, double destLng,
                      TransportType transportType) {
        try {
            String key = buildKey(originLat, originLng, destLat, destLng, transportType);
            masterRedisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis 캐시 삭제 실패 (이동시간): error={}", e.getMessage(), e);
        }
    }

    private String buildKey(double originLat, double originLng, double destLat, double destLng,
                            TransportType transportType) {
        return KEY_PREFIX
            + CoordinateHash.hash(originLat, originLng) + ":"
            + CoordinateHash.hash(destLat, destLng) + ":"
            + transportType.name();
    }

    private Duration resolveTtl(TransportType transportType) {
        return switch (transportType) {
            case CAR_PARKING, CAR_PICKUP -> Duration.ofMinutes(30);
            case TRANSIT -> Duration.ofHours(6);
            case WALKING -> null;
        };
    }
}
