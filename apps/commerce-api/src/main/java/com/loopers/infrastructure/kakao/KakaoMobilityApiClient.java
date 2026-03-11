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
public class KakaoMobilityApiClient {

    private static final String DIRECTIONS_PATH = "/v1/directions";

    private final RestClient kakaoMobilityRestClient;

    /**
     * Calculate car travel duration in minutes.
     * @param originLng origin longitude
     * @param originLat origin latitude
     * @param destLng destination longitude
     * @param destLat destination latitude
     * @return duration in minutes
     */
    public int calculateDuration(double originLng, double originLat, double destLng, double destLat) {
        try {
            String origin = originLng + "," + originLat;
            String destination = destLng + "," + destLat;

            KakaoMobilityResponse response = kakaoMobilityRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(DIRECTIONS_PATH)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .build())
                .retrieve()
                .body(KakaoMobilityResponse.class);

            if (response == null || response.routes() == null || response.routes().isEmpty()) {
                throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오모빌리티 경로 검색 결과가 없습니다.");
            }

            KakaoMobilityResponse.Route route = response.routes().get(0);
            if (route.resultCode() != 0) {
                log.warn("카카오모빌리티 경로 검색 실패: code={}, msg={}", route.resultCode(), route.resultMsg());
                throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "자동차 경로를 찾을 수 없습니다.");
            }

            int durationSeconds = route.summary().duration().value();
            return (int) Math.ceil(durationSeconds / 60.0);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("카카오모빌리티 API 클라이언트 오류: statusCode={}", e.getStatusCode());
                throw new CoreException(ErrorType.BAD_REQUEST, "자동차 이동시간 계산 요청이 잘못되었습니다.");
            }
            log.error("카카오모빌리티 API 서버 오류: statusCode={}", e.getStatusCode());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오모빌리티 서버에 일시적인 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            log.error("카카오모빌리티 API 타임아웃: {}", e.getMessage());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오모빌리티 서버 응답이 지연되고 있습니다.");
        }
    }
}
