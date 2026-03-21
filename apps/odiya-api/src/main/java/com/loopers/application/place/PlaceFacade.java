package com.loopers.application.place;

import com.loopers.domain.place.PlaceCacheRepository;
import com.loopers.infrastructure.kakao.KakaoLocalApiClient;
import com.loopers.infrastructure.kakao.KakaoLocalResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PlaceFacade {

    private final KakaoLocalApiClient kakaoLocalApiClient;
    private final PlaceCacheRepository placeCacheRepository;

    public PlaceCacheResult searchPlaces(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "검색어를 입력해 주세요.");
        }
        keyword = keyword.trim();
        if (keyword.length() > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "검색어는 최대 100자까지 입력할 수 있습니다.");
        }

        int safePage = Math.min(Math.max(page, 1), 45);
        int safeSize = Math.min(Math.max(size, 1), 15);

        // Cache-Aside: check cache first
        Optional<PlaceSearchResult> cached = placeCacheRepository.find(keyword, safePage, safeSize);
        if (cached.isPresent()) {
            return new PlaceCacheResult(cached.get(), true);
        }

        KakaoLocalResponse response = kakaoLocalApiClient.searchByKeyword(keyword, safePage, safeSize);

        List<PlaceInfo> places = response.documents().stream()
            .map(PlaceInfo::from)
            .filter(PlaceInfo::hasValidCoordinates)
            .toList();

        boolean hasNext = !response.meta().isEnd();

        PlaceSearchResult result = new PlaceSearchResult(places, hasNext);

        // Save to cache
        placeCacheRepository.save(keyword, safePage, safeSize, result);

        return new PlaceCacheResult(result, false);
    }
}
