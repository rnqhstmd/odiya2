package com.loopers.application.place;

import com.loopers.infrastructure.kakao.KakaoLocalApiClient;
import com.loopers.infrastructure.kakao.KakaoLocalResponse;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PlaceFacade {

    private final KakaoLocalApiClient kakaoLocalApiClient;

    public PlaceSearchResult searchPlaces(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "검색어를 입력해 주세요.");
        }
        keyword = keyword.trim();
        if (keyword.length() > 100) {
            throw new CoreException(ErrorType.BAD_REQUEST, "검색어는 최대 100자까지 입력할 수 있습니다.");
        }

        int safePage = Math.min(Math.max(page, 1), 45);
        int safeSize = Math.min(Math.max(size, 1), 15);

        KakaoLocalResponse response = kakaoLocalApiClient.searchByKeyword(keyword, safePage, safeSize);

        List<PlaceInfo> places = response.documents().stream()
            .map(PlaceInfo::from)
            .filter(PlaceInfo::hasValidCoordinates)
            .toList();

        boolean hasNext = !response.meta().isEnd();

        return new PlaceSearchResult(places, hasNext);
    }
}
