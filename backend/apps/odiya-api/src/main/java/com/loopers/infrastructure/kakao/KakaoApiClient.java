package com.loopers.infrastructure.kakao;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RequiredArgsConstructor
@Component
public class KakaoApiClient {

    private static final String USER_ME_PATH = "/v2/user/me";

    private final RestClient kakaoRestClient;

    public KakaoUserResponse fetchUser(String kakaoAccessToken) {
        try {
            KakaoApiResponse response = kakaoRestClient.get()
                .uri(USER_ME_PATH)
                .header("Authorization", "Bearer " + kakaoAccessToken)
                .retrieve()
                .body(KakaoApiResponse.class);

            if (response == null || response.id() == null) {
                throw new CoreException(ErrorType.UNAUTHORIZED, "카카오 인증에 실패했습니다.");
            }

            return KakaoUserResponse.from(response);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new CoreException(ErrorType.UNAUTHORIZED, "카카오 인증에 실패했습니다.");
            }
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("카카오 API 클라이언트 오류: statusCode={}", e.getStatusCode());
                throw new CoreException(ErrorType.BAD_REQUEST, "카카오 API 요청이 잘못되었습니다.");
            }
            log.error("카카오 API 서버 오류: statusCode={}", e.getStatusCode());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오 서버에 일시적인 오류가 발생했습니다.");
        } catch (ResourceAccessException e) {
            log.error("카카오 API 타임아웃: {}", e.getMessage());
            throw new CoreException(ErrorType.SERVICE_UNAVAILABLE, "카카오 서버 응답이 지연되고 있습니다.");
        }
    }
}
