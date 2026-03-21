package com.loopers.application.place;

import com.loopers.domain.place.PlaceCacheRepository;
import com.loopers.infrastructure.kakao.KakaoLocalApiClient;
import com.loopers.infrastructure.kakao.KakaoLocalResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PlaceFacadeTest {

    @Mock
    private KakaoLocalApiClient kakaoLocalApiClient;

    @Mock
    private PlaceCacheRepository placeCacheRepository;

    @InjectMocks
    private PlaceFacade placeFacade;

    @BeforeEach
    void setUp() {
        lenient().when(placeCacheRepository.find(anyString(), anyInt(), anyInt())).thenReturn(Optional.empty());
    }

    private KakaoLocalResponse.Document makeDocument(String id, String name, String x, String y) {
        return new KakaoLocalResponse.Document(id, name, "주소", "도로명주소", "카테고리", x, y);
    }

    private KakaoLocalResponse makeResponse(List<KakaoLocalResponse.Document> docs, boolean isEnd) {
        return new KakaoLocalResponse(docs, new KakaoLocalResponse.Meta(docs.size(), docs.size(), isEnd));
    }

    @DisplayName("장소를 검색할 때,")
    @Nested
    class SearchPlaces {

        @DisplayName("검색어가 null이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenKeywordIsNull() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> placeFacade.searchPlaces(null, 1, 5));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("검색어가 빈 문자열이면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenKeywordIsBlank() {
            // act
            CoreException result = assertThrows(CoreException.class,
                () -> placeFacade.searchPlaces("   ", 1, 5));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("검색어가 100자를 초과하면, BAD_REQUEST 예외가 발생한다.")
        @Test
        void throwsBadRequest_whenKeywordExceeds100Characters() {
            // arrange
            String longKeyword = "가".repeat(101);

            // act
            CoreException result = assertThrows(CoreException.class,
                () -> placeFacade.searchPlaces(longKeyword, 1, 5));

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("page가 46 이상이면, 45로 보정하여 검색한다.")
        @Test
        void clipsPageTo45_whenPageExceeds45() {
            // arrange
            KakaoLocalResponse response = makeResponse(List.of(
                makeDocument("1", "장소", "127.0276", "37.4979")
            ), true);
            given(kakaoLocalApiClient.searchByKeyword(anyString(), anyInt(), anyInt()))
                .willReturn(response);

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("강남", 100, 5);

            // assert
            assertThat(cacheResult).isNotNull();
            // page 100 → 45로 보정되어 API 호출됨 (예외 없이 정상 반환)
            assertThat(cacheResult.result().places()).hasSize(1);
        }

        @DisplayName("좌표가 0인 결과는 필터링된다.")
        @Test
        void filtersOutDocumentsWithZeroCoordinates() {
            // arrange
            KakaoLocalResponse response = makeResponse(List.of(
                makeDocument("1", "정상 장소", "127.0276", "37.4979"),
                makeDocument("2", "좌표없는 장소", "0", "0"),
                makeDocument("3", "경도만 0인 장소", "0", "37.5")
            ), true);
            given(kakaoLocalApiClient.searchByKeyword(anyString(), anyInt(), anyInt()))
                .willReturn(response);

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("강남", 1, 15);

            // assert
            assertThat(cacheResult.result().places()).hasSize(1);
            assertThat(cacheResult.result().places().get(0).name()).isEqualTo("정상 장소");
        }

        @DisplayName("정상적인 검색어와 페이지이면, 결과를 반환한다.")
        @Test
        void returnsResults_whenValidKeywordAndPage() {
            // arrange
            KakaoLocalResponse response = makeResponse(List.of(
                makeDocument("1", "강남역", "127.0276", "37.4979"),
                makeDocument("2", "강남구청", "127.0540", "37.5172")
            ), false);
            given(kakaoLocalApiClient.searchByKeyword(anyString(), anyInt(), anyInt()))
                .willReturn(response);

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("강남", 1, 5);

            // assert
            assertThat(cacheResult.result().places()).hasSize(2);
            assertThat(cacheResult.result().hasNext()).isTrue();
        }

        @DisplayName("카카오 API가 빈 결과를 반환하면, 빈 리스트와 hasNext=false를 반환한다.")
        @Test
        void returnsEmptyPlaces_whenNoResults() {
            // arrange
            KakaoLocalResponse response = makeResponse(List.of(), true);
            given(kakaoLocalApiClient.searchByKeyword(anyString(), anyInt(), anyInt()))
                .willReturn(response);

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("없는장소12345", 1, 5);

            // assert
            assertThat(cacheResult.result().places()).isEmpty();
            assertThat(cacheResult.result().hasNext()).isFalse();
        }

        @DisplayName("캐시히트시 API를 호출하지않는다.")
        @Test
        void doesNotCallApi_whenCacheHit() {
            // arrange
            PlaceSearchResult cached = new PlaceSearchResult(
                List.of(new PlaceInfo("1", "강남역", "주소", "도로명주소", "카테고리", 37.4979, 127.0276)),
                false);
            given(placeCacheRepository.find(anyString(), anyInt(), anyInt()))
                .willReturn(Optional.of(cached));

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("강남", 1, 5);

            // assert
            assertThat(cacheResult.result().places()).hasSize(1);
            assertThat(cacheResult.cacheHit()).isTrue();
            verify(kakaoLocalApiClient, never()).searchByKeyword(anyString(), anyInt(), anyInt());
        }

        @DisplayName("캐시미스시 API호출후 캐시에 저장한다.")
        @Test
        void callsApiAndSavesToCache_whenCacheMiss() {
            // arrange
            given(placeCacheRepository.find(anyString(), anyInt(), anyInt()))
                .willReturn(Optional.empty());
            KakaoLocalResponse response = makeResponse(List.of(
                makeDocument("1", "강남역", "127.0276", "37.4979")
            ), true);
            given(kakaoLocalApiClient.searchByKeyword(anyString(), anyInt(), anyInt()))
                .willReturn(response);

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("강남", 1, 5);

            // assert
            assertThat(cacheResult.result().places()).hasSize(1);
            assertThat(cacheResult.cacheHit()).isFalse();
            verify(kakaoLocalApiClient).searchByKeyword(anyString(), anyInt(), anyInt());
            verify(placeCacheRepository).save(anyString(), anyInt(), anyInt(), any());
        }

        @DisplayName("캐시히트시 cacheHit이 true이다.")
        @Test
        void cacheHitIsTrue_whenCacheHit() {
            // arrange
            PlaceSearchResult cached = new PlaceSearchResult(
                List.of(new PlaceInfo("1", "강남역", "주소", "도로명주소", "카테고리", 37.4979, 127.0276)),
                true);
            given(placeCacheRepository.find(anyString(), anyInt(), anyInt()))
                .willReturn(Optional.of(cached));

            // act
            PlaceCacheResult cacheResult = placeFacade.searchPlaces("강남", 1, 5);

            // assert
            assertThat(cacheResult.cacheHit()).isTrue();
        }
    }
}
