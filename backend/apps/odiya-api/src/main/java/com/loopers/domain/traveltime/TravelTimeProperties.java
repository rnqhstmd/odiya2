package com.loopers.domain.traveltime;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 이동시간 계산에 사용되는 비즈니스 상수.
 * {@code service.travel-time.*} prefix로 application.yml에서 오버라이드 가능.
 */
@ConfigurationProperties(prefix = "service.travel-time")
public record TravelTimeProperties(
    double walkingSpeedMetersPerMinute,
    double earthRadiusMeters,
    double withinDistanceMeters,
    double carDetourFactor,
    double carSpeedKmh,
    double transitDetourFactor,
    double transitSpeedKmh
) {

    /** 기본값 — 값이 application.yml에 누락되었을 때 사용되는 fallback. */
    public static TravelTimeProperties defaults() {
        return new TravelTimeProperties(
            80.0,
            6_371_000.0,
            50.0,
            1.4,
            40.0,
            1.5,
            30.0
        );
    }
}
