package com.loopers.interfaces.api.appointment;

import com.loopers.application.appointment.AppointmentDetailInfo;
import com.loopers.application.appointment.AppointmentFacade;
import com.loopers.application.appointment.AppointmentInfo;
import com.loopers.application.appointment.AppointmentListInfo;
import com.loopers.application.appointment.CalendarDayInfo;
import com.loopers.application.appointment.DepartureUpdateInfo;
import com.loopers.application.notification.NotificationFacade;
import com.loopers.config.security.LoginUser;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentV1Controller implements AppointmentV1ApiSpec {

    private final AppointmentFacade appointmentFacade;
    private final NotificationFacade notificationFacade;

    @PostMapping
    @Override
    public ApiResponse<AppointmentV1Dto.AppointmentResponse> createAppointment(
        @AuthenticationPrincipal LoginUser loginUser,
        @Valid @RequestBody AppointmentV1Dto.CreateAppointmentRequest request
    ) {
        AppointmentInfo info = appointmentFacade.create(
            loginUser.userId(),
            request.name(), request.placeName(), request.placeAddress(),
            request.latitude(), request.longitude(), request.dateTime(),
            request.participantIds(),
            request.transportType(),
            request.departurePlaceId()
        );
        return ApiResponse.success(AppointmentV1Dto.AppointmentResponse.from(info));
    }

    @GetMapping("/{id}")
    @Override
    public ApiResponse<AppointmentV1Dto.AppointmentDetailResponse> getAppointment(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id
    ) {
        AppointmentDetailInfo detail = appointmentFacade.getAppointmentDetail(id, loginUser.userId());
        return ApiResponse.success(AppointmentV1Dto.AppointmentDetailResponse.from(detail));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<AppointmentV1Dto.AppointmentListResponse> getMyAppointments(
        @AuthenticationPrincipal LoginUser loginUser,
        @RequestParam(required = false, defaultValue = "UPCOMING") String status,
        @RequestParam(required = false) Long cursor,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        AppointmentListInfo listInfo = appointmentFacade.getMyAppointments(
            loginUser.userId(), status, cursor, size);
        return ApiResponse.success(AppointmentV1Dto.AppointmentListResponse.from(listInfo));
    }

    @GetMapping("/calendar")
    @Override
    public ApiResponse<List<AppointmentV1Dto.CalendarDayResponse>> getCalendarData(
        @AuthenticationPrincipal LoginUser loginUser,
        @RequestParam Integer year,
        @RequestParam Integer month
    ) {
        List<CalendarDayInfo> days = appointmentFacade.getCalendarData(
            loginUser.userId(), year, month);
        List<AppointmentV1Dto.CalendarDayResponse> responses = days.stream()
            .map(AppointmentV1Dto.CalendarDayResponse::from)
            .toList();
        return ApiResponse.success(responses);
    }

    @PatchMapping("/{id}")
    @Override
    public ApiResponse<AppointmentV1Dto.AppointmentResponse> updateAppointment(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id,
        @Valid @RequestBody AppointmentV1Dto.UpdateAppointmentRequest request
    ) {
        AppointmentInfo info = appointmentFacade.updateAppointment(
            id, loginUser.userId(),
            request.name(), request.placeName(), request.placeAddress(),
            request.latitude(), request.longitude(), request.dateTime()
        );
        return ApiResponse.success(AppointmentV1Dto.AppointmentResponse.from(info));
    }

    @DeleteMapping("/{id}")
    @Override
    public ApiResponse<Void> cancelAppointment(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id
    ) {
        appointmentFacade.cancelAppointment(id, loginUser.userId());
        return ApiResponse.success();
    }

    @PostMapping("/{id}/accept")
    @Override
    public ApiResponse<Void> acceptInvitation(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id
    ) {
        appointmentFacade.acceptInvitation(id, loginUser.userId());
        return ApiResponse.success();
    }

    @PostMapping("/{id}/reject")
    @Override
    public ApiResponse<Void> rejectInvitation(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id
    ) {
        appointmentFacade.rejectInvitation(id, loginUser.userId());
        return ApiResponse.success();
    }

    @PostMapping("/{id}/invite")
    @Override
    public ApiResponse<Void> inviteParticipants(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id,
        @Valid @RequestBody AppointmentV1Dto.InviteRequest request
    ) {
        appointmentFacade.inviteParticipants(id, loginUser.userId(), request.userIds());
        return ApiResponse.success();
    }

    @PatchMapping("/{id}/departure")
    @Override
    public ApiResponse<AppointmentV1Dto.DepartureUpdateResponse> updateDeparture(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id,
        @Valid @RequestBody AppointmentV1Dto.UpdateDepartureRequest request
    ) {
        DepartureUpdateInfo info = appointmentFacade.updateDeparture(
            id, loginUser.userId(), request.departurePlaceId(), request.transportType());
        return ApiResponse.success(AppointmentV1Dto.DepartureUpdateResponse.from(info));
    }

    @PostMapping("/{id}/nudge")
    @Override
    public ApiResponse<Void> nudge(
        @AuthenticationPrincipal LoginUser loginUser,
        @PathVariable Long id,
        @Valid @RequestBody AppointmentV1Dto.NudgeRequest request
    ) {
        notificationFacade.nudge(id, loginUser.userId(), request.targetUserIds());
        return ApiResponse.success();
    }
}
