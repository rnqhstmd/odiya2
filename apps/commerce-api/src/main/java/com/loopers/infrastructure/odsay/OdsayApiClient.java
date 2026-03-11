package com.loopers.infrastructure.odsay;

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
public class OdsayApiClient {

    private static final String TRANSIT_PATH = "/v1/api/searchPubTransPathT";

    private final RestClient odsayRestClient;
    private final OdsayProperties odsayProperties;

    /**
     * Calculate public transit duration in minutes.
     * @param originLng origin longitude (SX)
     * @param originLat origin latitude (SY)
     * @param destLng destination longitude (EX)
     * @param destLat destination latitude (EY)
     * @return duration in minutes
     */
    public int calculateDuration(double originLng, double originLat, double destLng, double destLat) {
        try {
            OdsayResponse response = odsayRestClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path(TRANSIT_PATH)
                    .queryParam("SX", String.valueOf(originLng))
                    .queryParam("SY", String.valueOf(originLat))
                    .queryParam("EX", String.valueOf(destLng))
                    .queryParam("EY", String.valueOf(destLat))
                    // NOTE: ODsay API는 쿼리 파라미터로만 인증을 지원합니다. 액세스 로그에서 쿼리스트링을 제외하도록 인프라 설정이 필요합니다.
                    .queryParam("apiKey", odsayProperties.apiKey())
                    .build())
                .retrieve()
                .body(OdsayResponse.class);

            if (response == null || response.result() == null
                || response.result().path() == null || response.result().path().isEmpty()) {
                throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "대중교통 경로를 찾을 수 없습니다.");
            }

            return response.result().path().get(0).info().totalTime();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("ODsay API 클라이언트 오류: statusCode={}", e.getStatusCode());
                throw new CoreException(ErrorType.BAD_REQUEST, "대중교통 이동시간 계산 요청이 잘못되었습니다.");
            }
            log.error("ODsay API 서버 오류: statusCode={}", e.getStatusCode());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "ODsay 서버에 일시적인 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            log.error("ODsay API 타임아웃: {}", e.getMessage());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "ODsay 서버 응답이 지연되고 있습니다.");
        }
    }
}
