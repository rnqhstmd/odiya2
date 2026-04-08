package com.loopers.infrastructure.traveltime;

import com.loopers.domain.traveltime.port.ExternalTravelTimeProvider;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.infrastructure.kakao.KakaoMobilityApiClient;
import com.loopers.infrastructure.odsay.OdsayApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * {@link ExternalTravelTimeProvider}의 기본 어댑터 구현체.
 *
 * <p>이동수단에 따라 카카오모빌리티(자동차) 또는 ODsay(대중교통) API로 디스패치한다.
 * WALKING은 호출자(도메인 서비스)가 직접 처리하므로 이 구현체는 그 분기를 받지 않는다.
 *
 * <p>포트 시그니처는 도메인 규약대로 {@code (Lat, Lng)} 순서로 받지만, 외부 API 클라이언트는
 * 카카오/ODsay 모두 {@code (Lng, Lat)} 순서를 요구하므로 이 어댑터가 호출 시점에 순서를 뒤집어
 * 전달한다. 도메인 내부에 {@code (Lng, Lat)} 순서가 새어들지 않도록 변환 경계를 이곳으로 집약한다.
 */
@Component
@RequiredArgsConstructor
public class ExternalTravelTimeProviderImpl implements ExternalTravelTimeProvider {

    private final KakaoMobilityApiClient kakaoMobilityApiClient;
    private final OdsayApiClient odsayApiClient;

    @Override
    public int calculateDuration(TransportType transportType,
                                 double originLat, double originLng,
                                 double destLat, double destLng) {
        return switch (transportType) {
            case CAR_PARKING, CAR_PICKUP ->
                kakaoMobilityApiClient.calculateDuration(originLng, originLat, destLng, destLat);
            case TRANSIT ->
                odsayApiClient.calculateDuration(originLng, originLat, destLng, destLat);
            case WALKING ->
                throw new IllegalArgumentException(
                    "WALKING은 외부 API를 사용하지 않습니다. TravelTimeService 내부에서 처리됩니다.");
        };
    }
}
