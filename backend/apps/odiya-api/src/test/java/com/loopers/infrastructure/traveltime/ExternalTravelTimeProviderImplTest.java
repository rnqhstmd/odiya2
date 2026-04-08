package com.loopers.infrastructure.traveltime;

import com.loopers.domain.usersettings.TransportType;
import com.loopers.infrastructure.kakao.KakaoMobilityApiClient;
import com.loopers.infrastructure.odsay.OdsayApiClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * {@link ExternalTravelTimeProviderImpl}의 단위 테스트.
 *
 * <p>이 어댑터는 도메인 규약 {@code (Lat, Lng)}을 받아 외부 API 규약 {@code (Lng, Lat)}으로
 * 뒤집는 변환 경계이므로, 좌표 순서가 올바르게 swap되는지 구체 값으로 반드시 검증한다.
 * {@code anyDouble()} 매처만 쓰면 순서 뒤바뀜 회귀를 감지할 수 없다.
 */
@ExtendWith(MockitoExtension.class)
class ExternalTravelTimeProviderImplTest {

    // 강남역 ≈ (37.4979, 127.0276), 광화문 ≈ (37.5665, 126.9780)
    // 좌표 순서 swap이 정확한지 검증하려면 Lat/Lng 값이 명확히 구분되는 값을 사용한다.
    private static final double ORIGIN_LAT = 37.4979;
    private static final double ORIGIN_LNG = 127.0276;
    private static final double DEST_LAT = 37.5665;
    private static final double DEST_LNG = 126.9780;

    @Mock
    private KakaoMobilityApiClient kakaoMobilityApiClient;

    @Mock
    private OdsayApiClient odsayApiClient;

    @InjectMocks
    private ExternalTravelTimeProviderImpl provider;

    @DisplayName("calculateDuration을 호출할 때,")
    @Nested
    class CalculateDuration {

        @DisplayName("CAR_PARKING 이동수단이면, 카카오모빌리티에 (Lng, Lat) 순서로 전달하고 결과를 반환한다.")
        @Test
        void callsKakaoWithLngLatOrder_whenCarParking() {
            // arrange
            given(kakaoMobilityApiClient.calculateDuration(ORIGIN_LNG, ORIGIN_LAT, DEST_LNG, DEST_LAT))
                .willReturn(25);

            // act
            int duration = provider.calculateDuration(
                TransportType.CAR_PARKING, ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG);

            // assert
            assertThat(duration).isEqualTo(25);
            // 핵심: eq()로 구체값을 명시해 Lat/Lng swap이 일어났는지 검증
            verify(kakaoMobilityApiClient).calculateDuration(ORIGIN_LNG, ORIGIN_LAT, DEST_LNG, DEST_LAT);
            verifyNoInteractions(odsayApiClient);
        }

        @DisplayName("CAR_PICKUP 이동수단이면, 카카오모빌리티에 (Lng, Lat) 순서로 전달한다.")
        @Test
        void callsKakaoWithLngLatOrder_whenCarPickup() {
            // arrange
            given(kakaoMobilityApiClient.calculateDuration(ORIGIN_LNG, ORIGIN_LAT, DEST_LNG, DEST_LAT))
                .willReturn(30);

            // act
            int duration = provider.calculateDuration(
                TransportType.CAR_PICKUP, ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG);

            // assert
            assertThat(duration).isEqualTo(30);
            verify(kakaoMobilityApiClient).calculateDuration(ORIGIN_LNG, ORIGIN_LAT, DEST_LNG, DEST_LAT);
            verifyNoInteractions(odsayApiClient);
        }

        @DisplayName("TRANSIT 이동수단이면, ODsay에 (Lng, Lat) 순서로 전달하고 결과를 반환한다.")
        @Test
        void callsOdsayWithLngLatOrder_whenTransit() {
            // arrange
            given(odsayApiClient.calculateDuration(ORIGIN_LNG, ORIGIN_LAT, DEST_LNG, DEST_LAT))
                .willReturn(45);

            // act
            int duration = provider.calculateDuration(
                TransportType.TRANSIT, ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG);

            // assert
            assertThat(duration).isEqualTo(45);
            verify(odsayApiClient).calculateDuration(ORIGIN_LNG, ORIGIN_LAT, DEST_LNG, DEST_LAT);
            verifyNoInteractions(kakaoMobilityApiClient);
        }

        @DisplayName("WALKING 이동수단이면, IllegalArgumentException을 던지고 외부 API를 호출하지 않는다.")
        @Test
        void throwsIllegalArgument_whenWalking() {
            // act & assert
            assertThatThrownBy(() -> provider.calculateDuration(
                TransportType.WALKING, ORIGIN_LAT, ORIGIN_LNG, DEST_LAT, DEST_LNG))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("WALKING");

            verifyNoInteractions(kakaoMobilityApiClient);
            verifyNoInteractions(odsayApiClient);
        }

        @DisplayName("출발/목적 좌표가 서로 바뀌면, swap 전후 값이 호출에 반영된다.")
        @Test
        void preservesOriginAndDestinationOrder() {
            // arrange: origin과 dest를 일부러 바꿔서 호출
            given(kakaoMobilityApiClient.calculateDuration(DEST_LNG, DEST_LAT, ORIGIN_LNG, ORIGIN_LAT))
                .willReturn(15);

            // act: origin=(DEST_LAT, DEST_LNG), dest=(ORIGIN_LAT, ORIGIN_LNG)
            int duration = provider.calculateDuration(
                TransportType.CAR_PARKING, DEST_LAT, DEST_LNG, ORIGIN_LAT, ORIGIN_LNG);

            // assert: 어댑터가 origin/dest 역할을 섞지 않고 각자 swap만 수행했는지 확인
            assertThat(duration).isEqualTo(15);
            verify(kakaoMobilityApiClient).calculateDuration(DEST_LNG, DEST_LAT, ORIGIN_LNG, ORIGIN_LAT);
        }
    }
}
