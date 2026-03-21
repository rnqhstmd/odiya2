package com.loopers.interfaces.api.appointment;

import com.loopers.config.security.JwtProvider;
import com.loopers.domain.friend.Friendship;
import com.loopers.domain.user.User;
import com.loopers.domain.usersettings.TransportType;
import com.loopers.infrastructure.friend.FriendshipJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppointmentV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private FriendshipJpaRepository friendshipJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private User host;
    private User friend;
    private final ZonedDateTime futureDateTime = ZonedDateTime.now().plusDays(7);

    @BeforeEach
    void setUp() {
        host = userJpaRepository.save(User.create(11111L, "호스트", "https://example.com/host.jpg"));
        friend = userJpaRepository.save(User.create(22222L, "친구", "https://example.com/friend.jpg"));

        Friendship friendship = Friendship.createRequest(host, friend);
        friendship.accept();
        friendshipJpaRepository.save(friendship);
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("POST /api/v1/appointments")
    @Nested
    class CreateAppointment {

        @DisplayName("정상적인 요청이면, 200과 약속 정보를 반환한다.")
        @Test
        void returnsOk_whenValidRequest() {
            // arrange
            HttpHeaders headers = createAuthHeaders(host.getId());
            var request = new AppointmentV1Dto.CreateAppointmentRequest(
                "저녁 모임", "강남역", "서울 강남구 강남대로 396",
                37.4979, 127.0276, futureDateTime,
                List.of(friend.getId()), TransportType.TRANSIT, null);

            // act
            var responseType = new ParameterizedTypeReference<ApiResponse<AppointmentV1Dto.AppointmentResponse>>() {};
            ResponseEntity<ApiResponse<AppointmentV1Dto.AppointmentResponse>> response =
                testRestTemplate.exchange("/api/v1/appointments", HttpMethod.POST,
                    new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().name()).isEqualTo("저녁 모임"),
                () -> assertThat(response.getBody().data().status()).isEqualTo("PENDING"),
                () -> assertThat(response.getBody().data().participants()).hasSize(2)
            );
        }

        @DisplayName("인증이 없으면, 401을 반환한다.")
        @Test
        void returnsUnauthorized_whenNotAuthenticated() {
            // arrange
            var request = new AppointmentV1Dto.CreateAppointmentRequest(
                "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                List.of(friend.getId()), TransportType.TRANSIT, null);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments", HttpMethod.POST,
                    new HttpEntity<>(request), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @DisplayName("친구가 아닌 사용자를 초대하면, 400을 반환한다.")
        @Test
        void returnsBadRequest_whenInvitingNonFriend() {
            // arrange
            User stranger = userJpaRepository.save(User.create(33333L, "낯선이", "https://example.com/s.jpg"));
            HttpHeaders headers = createAuthHeaders(host.getId());
            var request = new AppointmentV1Dto.CreateAppointmentRequest(
                "모임", "강남역", "서울 강남구", 37.4979, 127.0276, futureDateTime,
                List.of(stranger.getId()), TransportType.TRANSIT, null);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments", HttpMethod.POST,
                    new HttpEntity<>(request, headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("GET /api/v1/appointments/{id}")
    @Nested
    class GetAppointment {

        @DisplayName("참여자이면, 200과 상세 정보를 반환한다.")
        @Test
        void returnsDetail_whenParticipant() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(host.getId());

            // act
            var responseType = new ParameterizedTypeReference<ApiResponse<AppointmentV1Dto.AppointmentDetailResponse>>() {};
            ResponseEntity<ApiResponse<AppointmentV1Dto.AppointmentDetailResponse>> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId, HttpMethod.GET,
                    new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().isHost()).isTrue(),
                () -> assertThat(response.getBody().data().myStatus()).isEqualTo("ACCEPTED")
            );
        }

        @DisplayName("참여자가 아니면, 404를 반환한다.")
        @Test
        void returnsNotFound_whenNotParticipant() {
            // arrange
            Long appointmentId = createAppointment();
            User stranger = userJpaRepository.save(User.create(33333L, "낯선이", "https://example.com/s.jpg"));
            HttpHeaders headers = createAuthHeaders(stranger.getId());

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @DisplayName("GET /api/v1/appointments/me")
    @Nested
    class GetMyAppointments {

        @DisplayName("UPCOMING 약속이 있으면, 200과 목록을 반환한다.")
        @Test
        void returnsUpcomingAppointments() {
            // arrange
            createAppointment();
            HttpHeaders headers = createAuthHeaders(host.getId());

            // act
            var responseType = new ParameterizedTypeReference<ApiResponse<AppointmentV1Dto.AppointmentListResponse>>() {};
            ResponseEntity<ApiResponse<AppointmentV1Dto.AppointmentListResponse>> response =
                testRestTemplate.exchange("/api/v1/appointments/me?status=UPCOMING", HttpMethod.GET,
                    new HttpEntity<>(headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().appointments()).hasSize(1),
                () -> assertThat(response.getBody().data().hasNext()).isFalse()
            );
        }

        @DisplayName("잘못된 status이면, 400을 반환한다.")
        @Test
        void returnsBadRequest_whenInvalidStatus() {
            // arrange
            HttpHeaders headers = createAuthHeaders(host.getId());

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/me?status=INVALID", HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("PATCH /api/v1/appointments/{id}")
    @Nested
    class UpdateAppointment {

        @DisplayName("호스트가 수정하면, 200을 반환한다.")
        @Test
        void returnsOk_whenHostUpdates() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(host.getId());
            var request = new AppointmentV1Dto.UpdateAppointmentRequest(
                "수정된 모임", null, null, null, null, null);

            // act
            var responseType = new ParameterizedTypeReference<ApiResponse<AppointmentV1Dto.AppointmentResponse>>() {};
            ResponseEntity<ApiResponse<AppointmentV1Dto.AppointmentResponse>> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId, HttpMethod.PATCH,
                    new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().name()).isEqualTo("수정된 모임")
            );
        }

        @DisplayName("호스트가 아니면, 401을 반환한다.")
        @Test
        void returnsUnauthorized_whenNotHost() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(friend.getId());
            var request = new AppointmentV1Dto.UpdateAppointmentRequest(
                "수정 시도", null, null, null, null, null);

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId, HttpMethod.PATCH,
                    new HttpEntity<>(request, headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @DisplayName("DELETE /api/v1/appointments/{id}")
    @Nested
    class CancelAppointment {

        @DisplayName("호스트가 취소하면, 200을 반환한다.")
        @Test
        void returnsOk_whenHostCancels() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(host.getId());

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId, HttpMethod.DELETE,
                    new HttpEntity<>(headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @DisplayName("POST /api/v1/appointments/{id}/accept")
    @Nested
    class AcceptInvitation {

        @DisplayName("초대받은 참여자가 수락하면, 200을 반환한다.")
        @Test
        void returnsOk_whenInviteeAccepts() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(friend.getId());

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId + "/accept",
                    HttpMethod.POST, new HttpEntity<>(headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @DisplayName("POST /api/v1/appointments/{id}/reject")
    @Nested
    class RejectInvitation {

        @DisplayName("초대받은 참여자가 거절하면, 200을 반환한다.")
        @Test
        void returnsOk_whenInviteeRejects() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(friend.getId());

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId + "/reject",
                    HttpMethod.POST, new HttpEntity<>(headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @DisplayName("POST /api/v1/appointments/{id}/invite")
    @Nested
    class InviteParticipants {

        @DisplayName("호스트가 친구를 추가 초대하면, 200을 반환한다.")
        @Test
        void returnsOk_whenHostInvitesFriend() {
            // arrange
            User friend2 = userJpaRepository.save(User.create(33333L, "친구2", "https://example.com/f2.jpg"));
            Friendship friendship2 = Friendship.createRequest(host, friend2);
            friendship2.accept();
            friendshipJpaRepository.save(friendship2);

            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(host.getId());
            var request = new AppointmentV1Dto.InviteRequest(List.of(friend2.getId()));

            // act
            ResponseEntity<String> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId + "/invite",
                    HttpMethod.POST, new HttpEntity<>(request, headers), String.class);

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @DisplayName("PATCH /api/v1/appointments/{id}/departure")
    @Nested
    class UpdateDeparture {

        @DisplayName("이동수단을 변경하면, 200과 변경 정보를 반환한다.")
        @Test
        void returnsOk_whenUpdatingTransportType() {
            // arrange
            Long appointmentId = createAppointment();
            HttpHeaders headers = createAuthHeaders(host.getId());
            var request = new AppointmentV1Dto.UpdateDepartureRequest(null, TransportType.CAR_PARKING);

            // act
            var responseType = new ParameterizedTypeReference<ApiResponse<AppointmentV1Dto.DepartureUpdateResponse>>() {};
            ResponseEntity<ApiResponse<AppointmentV1Dto.DepartureUpdateResponse>> response =
                testRestTemplate.exchange("/api/v1/appointments/" + appointmentId + "/departure",
                    HttpMethod.PATCH, new HttpEntity<>(request, headers), responseType);

            // assert
            assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                () -> assertThat(response.getBody().data().transportType()).isEqualTo("CAR_PARKING")
            );
        }
    }

    private Long createAppointment() {
        HttpHeaders headers = createAuthHeaders(host.getId());
        var request = new AppointmentV1Dto.CreateAppointmentRequest(
            "테스트 모임", "강남역", "서울 강남구 강남대로 396",
            37.4979, 127.0276, futureDateTime,
            List.of(friend.getId()), TransportType.TRANSIT, null);

        var responseType = new ParameterizedTypeReference<ApiResponse<AppointmentV1Dto.AppointmentResponse>>() {};
        ResponseEntity<ApiResponse<AppointmentV1Dto.AppointmentResponse>> response =
            testRestTemplate.exchange("/api/v1/appointments", HttpMethod.POST,
                new HttpEntity<>(request, headers), responseType);

        return response.getBody().data().id();
    }

    private HttpHeaders createAuthHeaders(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }
}
