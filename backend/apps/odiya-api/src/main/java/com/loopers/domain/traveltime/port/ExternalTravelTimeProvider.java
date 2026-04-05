package com.loopers.domain.traveltime.port;

import com.loopers.domain.usersettings.TransportType;

/**
 * 외부 이동시간 API 연동의 추상 포트.
 *
 * <p>도메인 레이어가 카카오모빌리티/ODsay 같은 구체 인프라 클라이언트를 직접 의존하지 않도록
 * 하기 위한 인터페이스다. 구현체는 {@code infrastructure} 레이어에 위치하며 DI로 주입된다.
 *
 * <p>{@link TransportType#WALKING}은 외부 API 없이 도메인 내부에서 Haversine 기반으로 계산하므로
 * 이 포트의 책임이 아니다 — {@code TravelTimeService}가 직접 처리한다.
 */
public interface ExternalTravelTimeProvider {

    /**
     * 출발지 → 목적지의 이동 소요시간(분)을 외부 API로 조회한다.
     *
     * @param transportType 이동수단 (CAR_PARKING, CAR_PICKUP, TRANSIT)
     * @param originLng 출발지 경도
     * @param originLat 출발지 위도
     * @param destLng 목적지 경도
     * @param destLat 목적지 위도
     * @return 소요 분
     * @throws RuntimeException 외부 API 호출 실패 시 (호출자가 fallback 처리)
     */
    int calculateDuration(TransportType transportType,
                          double originLng, double originLat,
                          double destLng, double destLat);
}
