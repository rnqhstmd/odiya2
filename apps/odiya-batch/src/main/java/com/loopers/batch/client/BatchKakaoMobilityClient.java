package com.loopers.batch.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class BatchKakaoMobilityClient {

    private static final String DIRECTIONS_PATH = "/v1/directions";

    private final RestClient batchKakaoMobilityRestClient;

    public int calculateDuration(double originLng, double originLat, double destLng, double destLat) {
        try {
            String origin = originLng + "," + originLat;
            String destination = destLng + "," + destLat;

            KakaoDirectionsResponse response = batchKakaoMobilityRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(DIRECTIONS_PATH)
                    .queryParam("origin", origin)
                    .queryParam("destination", destination)
                    .build())
                .retrieve()
                .body(KakaoDirectionsResponse.class);

            if (response == null || response.routes() == null || response.routes().isEmpty()) {
                throw new RuntimeException("카카오모빌리티 경로 검색 결과가 없습니다.");
            }

            KakaoDirectionsResponse.Route route = response.routes().get(0);
            if (route.resultCode() != 0) {
                throw new RuntimeException("자동차 경로를 찾을 수 없습니다: code=" + route.resultCode());
            }

            int durationSeconds = route.summary().duration().value();
            return (int) Math.ceil(durationSeconds / 60.0);
        } catch (RestClientResponseException e) {
            log.warn("카카오모빌리티 API 오류: statusCode={}", e.getStatusCode());
            throw new RuntimeException("카카오모빌리티 API 호출 실패: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.warn("카카오모빌리티 API 타임아웃: {}", e.getMessage());
            throw new RuntimeException("카카오모빌리티 API 타임아웃", e);
        }
    }

    public record KakaoDirectionsResponse(
        @JsonProperty("trans_id") String transId,
        List<Route> routes
    ) {
        public record Route(
            @JsonProperty("result_code") int resultCode,
            @JsonProperty("result_msg") String resultMsg,
            Summary summary
        ) {}

        public record Summary(
            Distance distance,
            Duration duration
        ) {}

        public record Distance(int value, String text) {}
        public record Duration(int value, String text) {}
    }
}
