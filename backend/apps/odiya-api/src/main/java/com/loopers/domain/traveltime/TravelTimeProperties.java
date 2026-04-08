package com.loopers.domain.traveltime;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * 이동시간 계산에 사용되는 비즈니스 상수.
 *
 * <p>{@code service.travel-time.*} prefix로 {@code application.yml}에서 오버라이드 가능하며,
 * 값이 누락된 경우 {@link DefaultValue}로 주입된 기본값이 적용된다. 이는 기본값이 {@code 0.0}으로
 * 바인딩되어 분모로 사용되는 상수(속도 등)가 {@code Infinity}로 계산되는 사고를 방지한다.
 *
 * <p>또한 모든 필드에 {@link Positive @Positive} 제약을 걸어 {@code yml}에 실수로 {@code 0}
 * 또는 음수 값이 설정되는 경우 Spring 컨텍스트 부팅 시점에 {@code ConstraintViolationException}으로
 * fail-fast한다. 이로써 누락(= @DefaultValue)과 잘못된 값(= @Positive) 두 가지 실수 모두를
 * 런타임 이전에 차단한다.
 */
@Validated
@ConfigurationProperties(prefix = "service.travel-time")
public record TravelTimeProperties(
    @DefaultValue("80.0")      @Positive double walkingSpeedMetersPerMinute,
    @DefaultValue("6371000.0") @Positive double earthRadiusMeters,
    @DefaultValue("50.0")      @Positive double withinDistanceMeters,
    @DefaultValue("1.4")       @Positive double carDetourFactor,
    @DefaultValue("40.0")      @Positive double carSpeedKmh,
    @DefaultValue("1.5")       @Positive double transitDetourFactor,
    @DefaultValue("30.0")      @Positive double transitSpeedKmh
) {
}
