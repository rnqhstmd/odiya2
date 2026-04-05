package com.loopers.domain.traveltime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TravelTimeProperties}의 {@code @DefaultValue} 바인딩 동작을 검증하는 테스트.
 *
 * <p>{@code application.yml}에 값이 누락되었을 때 레코드 필드가 {@code 0.0}으로 바인딩되지 않고
 * {@code @DefaultValue}로 선언된 값으로 채워지는지 확인한다. 이 검증이 없으면 누군가 실수로
 * {@code application.yml}에서 키를 삭제해도 테스트는 통과하지만 런타임에 이동시간이
 * {@code Infinity}로 계산되는 사고로 이어질 수 있다.
 */
class TravelTimePropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(org.springframework.boot.autoconfigure.AutoConfigurations.of(
            ConfigurationPropertiesAutoConfiguration.class))
        .withUserConfiguration(TestConfig.class);

    @EnableConfigurationProperties(TravelTimeProperties.class)
    static class TestConfig {
    }

    @DisplayName("application.yml에 값이 없는 상태에서 바인딩할 때,")
    @Nested
    class WhenNoPropertiesProvided {

        @DisplayName("모든 필드가 @DefaultValue로 선언된 값으로 채워진다.")
        @Test
        void allFieldsFilledWithDefaultValues() {
            contextRunner.run(context -> {
                TravelTimeProperties properties = context.getBean(TravelTimeProperties.class);

                assertThat(properties.walkingSpeedMetersPerMinute()).isEqualTo(80.0);
                assertThat(properties.earthRadiusMeters()).isEqualTo(6_371_000.0);
                assertThat(properties.withinDistanceMeters()).isEqualTo(50.0);
                assertThat(properties.carDetourFactor()).isEqualTo(1.4);
                assertThat(properties.carSpeedKmh()).isEqualTo(40.0);
                assertThat(properties.transitDetourFactor()).isEqualTo(1.5);
                assertThat(properties.transitSpeedKmh()).isEqualTo(30.0);
            });
        }

        @DisplayName("분모로 사용되는 속도 상수는 0.0으로 바인딩되지 않는다 (Infinity 방지).")
        @Test
        void speedFieldsNeverZero() {
            contextRunner.run(context -> {
                TravelTimeProperties properties = context.getBean(TravelTimeProperties.class);

                // 0.0으로 나누면 Infinity가 되므로 반드시 양수여야 한다
                assertThat(properties.walkingSpeedMetersPerMinute()).isGreaterThan(0.0);
                assertThat(properties.carSpeedKmh()).isGreaterThan(0.0);
                assertThat(properties.transitSpeedKmh()).isGreaterThan(0.0);
            });
        }
    }

    @DisplayName("application.yml에 일부 값만 제공될 때,")
    @Nested
    class WhenPartialPropertiesProvided {

        @DisplayName("제공된 값은 오버라이드되고 나머지는 기본값이 유지된다.")
        @Test
        void providedValuesOverrideDefaults() {
            contextRunner
                .withPropertyValues(
                    "service.travel-time.walking-speed-meters-per-minute=100.0",
                    "service.travel-time.car-speed-kmh=60.0"
                )
                .run(context -> {
                    TravelTimeProperties properties = context.getBean(TravelTimeProperties.class);

                    // 오버라이드된 값
                    assertThat(properties.walkingSpeedMetersPerMinute()).isEqualTo(100.0);
                    assertThat(properties.carSpeedKmh()).isEqualTo(60.0);

                    // 기본값 유지
                    assertThat(properties.earthRadiusMeters()).isEqualTo(6_371_000.0);
                    assertThat(properties.withinDistanceMeters()).isEqualTo(50.0);
                    assertThat(properties.carDetourFactor()).isEqualTo(1.4);
                    assertThat(properties.transitDetourFactor()).isEqualTo(1.5);
                    assertThat(properties.transitSpeedKmh()).isEqualTo(30.0);
                });
        }
    }
}
