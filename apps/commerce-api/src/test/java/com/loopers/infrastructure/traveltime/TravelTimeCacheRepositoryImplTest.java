package com.loopers.infrastructure.traveltime;

import com.loopers.domain.usersettings.TransportType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TravelTimeCacheRepositoryImplTest {

    @Mock
    private RedisTemplate<String, String> replicaRedisTemplate;

    @Mock
    private RedisTemplate<String, String> masterRedisTemplate;

    @Mock
    private ValueOperations<String, String> replicaValueOps;

    @Mock
    private ValueOperations<String, String> masterValueOps;

    private TravelTimeCacheRepositoryImpl travelTimeCacheRepository;

    private void initRepository() {
        travelTimeCacheRepository = new TravelTimeCacheRepositoryImpl(replicaRedisTemplate, masterRedisTemplate);
    }

    @DisplayName("find를 호출할 때,")
    @Nested
    class Find {

        @DisplayName("캐시히트시 값을 반환한다.")
        @Test
        void find_캐시히트시_값을_반환한다() {
            // arrange
            given(replicaRedisTemplate.opsForValue()).willReturn(replicaValueOps);
            given(replicaValueOps.get(anyString())).willReturn("30");
            initRepository();

            // act
            Optional<Integer> result = travelTimeCacheRepository.find(37.4979, 127.0276, 37.5665, 126.9780, TransportType.TRANSIT);

            // assert
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(30);
        }

        @DisplayName("캐시미스시 빈값을 반환한다.")
        @Test
        void find_캐시미스시_빈값을_반환한다() {
            // arrange
            given(replicaRedisTemplate.opsForValue()).willReturn(replicaValueOps);
            given(replicaValueOps.get(anyString())).willReturn(null);
            initRepository();

            // act
            Optional<Integer> result = travelTimeCacheRepository.find(37.4979, 127.0276, 37.5665, 126.9780, TransportType.TRANSIT);

            // assert
            assertThat(result).isEmpty();
        }

        @DisplayName("Redis장애시 빈값을 반환한다.")
        @Test
        void find_Redis장애시_빈값을_반환한다() {
            // arrange
            given(replicaRedisTemplate.opsForValue()).willThrow(new RuntimeException("Redis connection refused"));
            initRepository();

            // act
            Optional<Integer> result = travelTimeCacheRepository.find(37.4979, 127.0276, 37.5665, 126.9780, TransportType.TRANSIT);

            // assert
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("save를 호출할 때,")
    @Nested
    class Save {

        @DisplayName("이동수단별 TTL로 저장한다.")
        @Test
        void save_이동수단별_TTL로_저장한다() {
            // arrange
            given(masterRedisTemplate.opsForValue()).willReturn(masterValueOps);
            initRepository();

            // act - CAR: 30분 TTL
            travelTimeCacheRepository.save(37.4979, 127.0276, 37.5665, 126.9780, TransportType.CAR_PARKING, 30);
            // assert
            verify(masterValueOps).set(anyString(), eq("30"), eq(Duration.ofMinutes(30)));

            // act - TRANSIT: 6시간 TTL
            travelTimeCacheRepository.save(37.4979, 127.0276, 37.5665, 126.9780, TransportType.TRANSIT, 45);
            // assert
            verify(masterValueOps).set(anyString(), eq("45"), eq(Duration.ofHours(6)));

            // act - WALKING: 무기한 (TTL 없음)
            travelTimeCacheRepository.save(37.4979, 127.0276, 37.5665, 126.9780, TransportType.WALKING, 15);
            // assert
            verify(masterValueOps).set(anyString(), eq("15"));
        }

        @DisplayName("Redis장애시 예외를 던지지않는다.")
        @Test
        void save_Redis장애시_예외를_던지지않는다() {
            // arrange
            given(masterRedisTemplate.opsForValue()).willThrow(new RuntimeException("Redis connection refused"));
            initRepository();

            // act & assert - 예외가 전파되지 않아야 함
            travelTimeCacheRepository.save(37.4979, 127.0276, 37.5665, 126.9780, TransportType.TRANSIT, 30);
        }
    }

    @DisplayName("evict를 호출할 때,")
    @Nested
    class Evict {

        @DisplayName("캐시를 삭제한다.")
        @Test
        void evict_캐시를_삭제한다() {
            // arrange
            given(masterRedisTemplate.delete(anyString())).willReturn(true);
            initRepository();

            // act
            travelTimeCacheRepository.evict(37.4979, 127.0276, 37.5665, 126.9780, TransportType.TRANSIT);

            // assert
            verify(masterRedisTemplate).delete(anyString());
        }
    }
}
