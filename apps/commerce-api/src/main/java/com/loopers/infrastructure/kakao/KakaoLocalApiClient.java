package com.loopers.infrastructure.kakao;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoLocalApiClient {

    private static final String KEYWORD_SEARCH_PATH = "/v2/local/search/keyword.json";

    private final RestClient kakaoLocalRestClient;

    public KakaoLocalResponse searchByKeyword(String keyword, int page, int size) {
        try {
            KakaoLocalResponse response = kakaoLocalRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(KEYWORD_SEARCH_PATH)
                    .queryParam("query", keyword)
                    .queryParam("page", page)
                    .queryParam("size", size)
                    .build())
                .retrieve()
                .body(KakaoLocalResponse.class);

            if (response == null) {
                throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오 장소 검색 응답이 비어 있습니다.");
            }

            return response;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("카카오 로컬 API 클라이언트 오류: statusCode={}", e.getStatusCode());
                throw new CoreException(ErrorType.BAD_REQUEST, "장소 검색 요청이 잘못되었습니다.");
            }
            log.error("카카오 로컬 API 서버 오류: statusCode={}", e.getStatusCode());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오 장소 검색 서버에 일시적인 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            log.error("카카오 로컬 API 타임아웃: {}", e.getMessage());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오 장소 검색 서버 응답이 지연되고 있습니다.");
        }
    }
}
