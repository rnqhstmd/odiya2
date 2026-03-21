package com.loopers.infrastructure.place;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.application.place.PlaceInfo;
import com.loopers.application.place.PlaceSearchResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceCacheRepositoryImplTest {

    @Mock
    private RedisTemplate<String, String> replicaRedisTemplate;

    @Mock
    private RedisTemplate<String, String> masterRedisTemplate;

    @Mock
    private ValueOperations<String, String> replicaValueOps;

    @Mock
    private ValueOperations<String, String> masterValueOps;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private PlaceCacheRepositoryImpl placeCacheRepository;

    private void initRepository() {
        placeCacheRepository = new PlaceCacheRepositoryImpl(replicaRedisTemplate, masterRedisTemplate, objectMapper);
    }

    private PlaceSearchResult makePlaceSearchResult() {
        PlaceInfo place = new PlaceInfo("1", "강남역", "서울 강남구", "서울 강남구 강남대로", "지하철역", 37.4979, 127.0276);
        return new PlaceSearchResult(List.of(place), false);
    }

    @DisplayName("find를 호출할 때,")
    @Nested
    class Find {

        @DisplayName("캐시히트시 결과를 반환한다.")
        @Test
        void find_캐시히트시_결과를_반환한다() throws JsonProcessingException {
            // arrange
            PlaceSearchResult expected = makePlaceSearchResult();
            String json = objectMapper.writeValueAsString(expected);
            given(replicaRedisTemplate.opsForValue()).willReturn(replicaValueOps);
            given(replicaValueOps.get(anyString())).willReturn(json);
            initRepository();

            // act
            Optional<PlaceSearchResult> result = placeCacheRepository.find("강남", 1, 5);

            // assert
            assertThat(result).isPresent();
            assertThat(result.get().places()).hasSize(1);
            assertThat(result.get().places().get(0).name()).isEqualTo("강남역");
        }

        @DisplayName("캐시미스시 빈값을 반환한다.")
        @Test
        void find_캐시미스시_빈값을_반환한다() {
            // arrange
            given(replicaRedisTemplate.opsForValue()).willReturn(replicaValueOps);
            given(replicaValueOps.get(anyString())).willReturn(null);
            initRepository();

            // act
            Optional<PlaceSearchResult> result = placeCacheRepository.find("강남", 1, 5);

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
            Optional<PlaceSearchResult> result = placeCacheRepository.find("강남", 1, 5);

            // assert
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("save를 호출할 때,")
    @Nested
    class Save {

        @DisplayName("24시간 TTL로 저장한다.")
        @Test
        void save_24시간_TTL로_저장한다() {
            // arrange
            given(masterRedisTemplate.opsForValue()).willReturn(masterValueOps);
            initRepository();
            PlaceSearchResult searchResult = makePlaceSearchResult();

            // act
            placeCacheRepository.save("강남", 1, 5, searchResult);

            // assert
            verify(masterValueOps).set(anyString(), anyString(), eq(Duration.ofHours(24)));
        }

        @DisplayName("Redis장애시 예외를 던지지않는다.")
        @Test
        void save_Redis장애시_예외를_던지지않는다() {
            // arrange
            given(masterRedisTemplate.opsForValue()).willThrow(new RuntimeException("Redis connection refused"));
            initRepository();
            PlaceSearchResult searchResult = makePlaceSearchResult();

            // act & assert - 예외가 전파되지 않아야 함
            placeCacheRepository.save("강남", 1, 5, searchResult);
        }
    }

    @DisplayName("hashKeyword를 호출할 때,")
    @Nested
    class HashKeyword {

        @DisplayName("동일검색어는 동일해시를 생성한다.")
        @Test
        void hashKeyword_동일검색어는_동일해시를_생성한다() {
            // arrange
            given(masterRedisTemplate.opsForValue()).willReturn(masterValueOps);
            initRepository();
            PlaceSearchResult searchResult = makePlaceSearchResult();

            // act: 동일 keyword, page, size로 save 2번 → 동일 key로 저장됨을 검증
            placeCacheRepository.save("강남역", 1, 5, searchResult);
            placeCacheRepository.save("강남역", 1, 5, searchResult);

            // assert: 동일한 key로 2번 호출됨
            verify(masterValueOps, org.mockito.Mockito.times(2))
                .set(anyString(), anyString(), eq(Duration.ofHours(24)));
        }

        @DisplayName("다른검색어는 다른해시를 생성한다.")
        @Test
        void hashKeyword_다른검색어는_다른해시를_생성한다() {
            // arrange
            given(replicaRedisTemplate.opsForValue()).willReturn(replicaValueOps);
            given(replicaValueOps.get(anyString())).willReturn(null);
            initRepository();

            // act: 서로 다른 keyword로 find → 서로 다른 key가 조회됨
            placeCacheRepository.find("강남역", 1, 5);
            placeCacheRepository.find("홍대입구", 1, 5);

            // assert: opsForValue().get()이 서로 다른 key로 2번 호출됨
            org.mockito.ArgumentCaptor<String> keyCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
            verify(replicaValueOps, org.mockito.Mockito.times(2)).get(keyCaptor.capture());
            List<String> keys = keyCaptor.getAllValues();
            assertThat(keys.get(0)).isNotEqualTo(keys.get(1));
        }
    }
}
