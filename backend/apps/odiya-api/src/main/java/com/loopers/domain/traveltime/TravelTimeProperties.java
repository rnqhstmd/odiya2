package com.loopers.domain.traveltime;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 이동시간 계산에 사용되는 비즈니스 상수.
 * {@code service.travel-time.*} prefix로 application.yml에서 오버라이드 가능하며,
 * 값이 누락된 경우 {@link DefaultValue}로 주입된 기본값이 적용된다.
 * 기본값이 0.0으로 바인딩되어 분모로 사용되는 상수(속도 등)가 {@code Infinity}로
 * 계산되는 사고를 방지한다.
 */
@ConfigurationProperties(prefix = "service.travel-time")
public record TravelTimeProperties(
    @DefaultValue("80.0")     double walkingSpeedMetersPerMinute,
    @DefaultValue("6371000.0") double earthRadiusMeters,
    @DefaultValue("50.0")     double withinDistanceMeters,
    @DefaultValue("1.4")      double carDetourFactor,
    @DefaultValue("40.0")     double carSpeedKmh,
    @DefaultValue("1.5")      double transitDetourFactor,
    @DefaultValue("30.0")     double transitSpeedKmh
) {

    /** 테스트용 기본값 팩토리 — 단위 테스트에서 수동 주입할 때 사용한다. */
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
