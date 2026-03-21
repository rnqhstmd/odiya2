package com.loopers.application.departureplace;

import com.loopers.domain.departureplace.DeparturePlace;
import com.loopers.domain.departureplace.DeparturePlaceService;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeparturePlaceFacadeTest {

    @Mock
    private DeparturePlaceService departurePlaceService;

    @Mock
    private UserService userService;

    @InjectMocks
    private DeparturePlaceFacade departurePlaceFacade;

    @DisplayName("create 테스트")
    @Nested
    class Create {

        @DisplayName("정상: 사용자 조회 후 출발지 생성이 위임된다.")
        @Test
        void create_정상() {
            // arrange
            long userId = 1L;
            String label = "집";
            String address = "서울 강남구 테헤란로 1";
            double latitude = 37.4979;
            double longitude = 127.0276;

            User user = mock(User.class);
            DeparturePlace place = mock(DeparturePlace.class);
            given(place.getId()).willReturn(10L);
            given(place.getLabel()).willReturn(label);
            given(place.getAddress()).willReturn(address);
            given(place.getLatitude()).willReturn(latitude);
            given(place.getLongitude()).willReturn(longitude);

            given(userService.getUser(userId)).willReturn(user);
            given(departurePlaceService.create(user, label, address, latitude, longitude)).willReturn(place);

            // act
            DeparturePlaceInfo result = departurePlaceFacade.create(userId, label, address, latitude, longitude);

            // assert
            verify(userService).getUser(userId);
            verify(departurePlaceService).create(user, label, address, latitude, longitude);
            assertThat(result.label()).isEqualTo(label);
            assertThat(result.address()).isEqualTo(address);
        }
    }

    @DisplayName("update 테스트")
    @Nested
    class Update {

        @DisplayName("정상: 출발지 수정이 위임된다.")
        @Test
        void update_정상() {
            // arrange
            long userId = 1L;
            long placeId = 10L;
            String newLabel = "회사";
            String newAddress = "서울 중구 을지로 1";
            double newLatitude = 37.5665;
            double newLongitude = 126.9780;

            DeparturePlace updatedPlace = mock(DeparturePlace.class);
            given(updatedPlace.getId()).willReturn(placeId);
            given(updatedPlace.getLabel()).willReturn(newLabel);
            given(updatedPlace.getAddress()).willReturn(newAddress);
            given(updatedPlace.getLatitude()).willReturn(newLatitude);
            given(updatedPlace.getLongitude()).willReturn(newLongitude);

            given(departurePlaceService.update(placeId, userId, newLabel, newAddress, newLatitude, newLongitude))
                .willReturn(updatedPlace);

            // act
            DeparturePlaceInfo result = departurePlaceFacade.update(userId, placeId, newLabel, newAddress, newLatitude, newLongitude);

            // assert
            verify(departurePlaceService).update(placeId, userId, newLabel, newAddress, newLatitude, newLongitude);
            assertThat(result.label()).isEqualTo(newLabel);
            assertThat(result.address()).isEqualTo(newAddress);
        }
    }

    @DisplayName("delete 테스트")
    @Nested
    class Delete {

        @DisplayName("정상: 출발지 삭제가 위임된다.")
        @Test
        void delete_정상() {
            // arrange
            long userId = 1L;
            long placeId = 10L;

            // act
            departurePlaceFacade.delete(userId, placeId);

            // assert
            verify(departurePlaceService).delete(placeId, userId);
        }
    }

    @DisplayName("getMyDeparturePlaces 테스트")
    @Nested
    class GetMyDeparturePlaces {

        @DisplayName("정상: 출발지 목록 조회가 위임된다.")
        @Test
        void getMyDeparturePlaces_정상() {
            // arrange
            long userId = 1L;

            DeparturePlace place1 = mock(DeparturePlace.class);
            given(place1.getId()).willReturn(1L);
            given(place1.getLabel()).willReturn("집");
            given(place1.getAddress()).willReturn("서울 강남구 테헤란로 1");
            given(place1.getLatitude()).willReturn(37.4979);
            given(place1.getLongitude()).willReturn(127.0276);

            DeparturePlace place2 = mock(DeparturePlace.class);
            given(place2.getId()).willReturn(2L);
            given(place2.getLabel()).willReturn("회사");
            given(place2.getAddress()).willReturn("서울 중구 을지로 1");
            given(place2.getLatitude()).willReturn(37.5665);
            given(place2.getLongitude()).willReturn(126.9780);

            given(departurePlaceService.getAllByUserId(userId)).willReturn(List.of(place1, place2));

            // act
            List<DeparturePlaceInfo> result = departurePlaceFacade.getMyDeparturePlaces(userId);

            // assert
            verify(departurePlaceService).getAllByUserId(userId);
            assertThat(result).hasSize(2);
            assertThat(result.get(0).label()).isEqualTo("집");
            assertThat(result.get(1).label()).isEqualTo("회사");
        }
    }
}
