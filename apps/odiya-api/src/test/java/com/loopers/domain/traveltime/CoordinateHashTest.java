package com.loopers.domain.traveltime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinateHashTest {

    @DisplayName("hash를 호출할 때,")
    @Nested
    class Hash {

        @DisplayName("소수점 4자리로 반올림한다.")
        @Test
        void hash_소수점_4자리로_반올림한다() {
            // act
            String result = CoordinateHash.hash(37.49791234, 127.02761234);

            // assert
            assertThat(result).isEqualTo("37.4979,127.0276");
        }

        @DisplayName("동일좌표는 동일해시를 생성한다.")
        @Test
        void hash_동일좌표는_동일해시를_생성한다() {
            // act
            String hash1 = CoordinateHash.hash(37.4979, 127.0276);
            String hash2 = CoordinateHash.hash(37.4979, 127.0276);

            // assert
            assertThat(hash1).isEqualTo(hash2);
        }

        @DisplayName("11m이내 좌표는 동일해시를 생성한다.")
        @Test
        void hash_11m이내_좌표는_동일해시를_생성한다() {
            // arrange: 소수점 4자리 = ~11m 범위, 4자리 반올림 시 동일
            double lat1 = 37.49791;
            double lng1 = 127.02761;
            double lat2 = 37.49794; // 0.00003 차이 → 반올림 후 동일
            double lng2 = 127.02764;

            // act
            String hash1 = CoordinateHash.hash(lat1, lng1);
            String hash2 = CoordinateHash.hash(lat2, lng2);

            // assert
            assertThat(hash1).isEqualTo(hash2);
        }

        @DisplayName("다른좌표는 다른해시를 생성한다.")
        @Test
        void hash_다른좌표는_다른해시를_생성한다() {
            // act
            String hash1 = CoordinateHash.hash(37.4979, 127.0276);
            String hash2 = CoordinateHash.hash(37.5665, 126.9780);

            // assert
            assertThat(hash1).isNotEqualTo(hash2);
        }
    }
}
