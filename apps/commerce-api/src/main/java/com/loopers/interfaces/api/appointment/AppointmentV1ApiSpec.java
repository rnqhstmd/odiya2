package com.loopers.interfaces.api.appointment;

import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "Appointment V1 API", description = "약속 관리 API")
public interface AppointmentV1ApiSpec {

    @Operation(summary = "약속 생성", description = "새로운 약속을 생성합니다.")
    ApiResponse<AppointmentV1Dto.AppointmentResponse> createAppointment(
        LoginUser loginUser, AppointmentV1Dto.CreateAppointmentRequest request);

    @Operation(summary = "약속 상세 조회", description = "약속 상세 정보를 조회합니다.")
    ApiResponse<AppointmentV1Dto.AppointmentDetailResponse> getAppointment(
        LoginUser loginUser, Long id);

    @Operation(summary = "내 약속 목록 조회", description = "내 약속 목록을 커서 기반 페이징으로 조회합니다.")
    ApiResponse<AppointmentV1Dto.AppointmentListResponse> getMyAppointments(
        LoginUser loginUser, String status, Long cursor, Integer size);

    @Operation(summary = "캘린더 데이터 조회", description = "해당 월의 약속 캘린더 데이터를 조회합니다.")
    ApiResponse<List<AppointmentV1Dto.CalendarDayResponse>> getCalendarData(
        LoginUser loginUser, Integer year, Integer month);

    @Operation(summary = "약속 수정", description = "약속 정보를 수정합니다. (호스트 전용)")
    ApiResponse<AppointmentV1Dto.AppointmentResponse> updateAppointment(
        LoginUser loginUser, Long id, AppointmentV1Dto.UpdateAppointmentRequest request);

    @Operation(summary = "약속 취소", description = "약속을 취소합니다. (호스트 전용)")
    ApiResponse<Void> cancelAppointment(LoginUser loginUser, Long id);

    @Operation(summary = "초대 수락", description = "약속 초대를 수락합니다.")
    ApiResponse<Void> acceptInvitation(LoginUser loginUser, Long id);

    @Operation(summary = "초대 거절", description = "약속 초대를 거절합니다.")
    ApiResponse<Void> rejectInvitation(LoginUser loginUser, Long id);

    @Operation(summary = "추가 초대", description = "약속에 참여자를 추가 초대합니다. (호스트 전용)")
    ApiResponse<Void> inviteParticipants(
        LoginUser loginUser, Long id, AppointmentV1Dto.InviteRequest request);

    @Operation(summary = "출발지 설정/변경", description = "약속 출발지와 이동수단을 설정/변경합니다.")
    ApiResponse<AppointmentV1Dto.DepartureUpdateResponse> updateDeparture(
        LoginUser loginUser, Long id, AppointmentV1Dto.UpdateDepartureRequest request);
}
