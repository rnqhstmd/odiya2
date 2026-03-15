package com.loopers.interfaces.api.appointment;

import com.loopers.application.appointment.AppointmentDetailInfo;
import com.loopers.application.appointment.AppointmentInfo;
import com.loopers.application.appointment.AppointmentListInfo;
import com.loopers.application.appointment.CalendarAppointmentInfo;
import com.loopers.application.appointment.CalendarDayInfo;
import com.loopers.application.appointment.DepartureUpdateInfo;
import com.loopers.application.appointment.ParticipantInfo;
import com.loopers.domain.usersettings.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

public class AppointmentV1Dto {

    public record CreateAppointmentRequest(
        @NotBlank String name,
        @NotBlank String placeName,
        @NotBlank String placeAddress,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotNull ZonedDateTime dateTime,
        @NotNull @Size(max = 30, message = "참여자는 최대 30명까지 초대할 수 있습니다.") List<Long> participantIds,
        @NotNull TransportType transportType,
        Long departurePlaceId
    ) {}

    public record UpdateAppointmentRequest(
        String name,
        String placeName,
        String placeAddress,
        Double latitude,
        Double longitude,
        ZonedDateTime dateTime
    ) {}

    public record InviteRequest(
        @NotNull @Size(max = 30, message = "한 번에 최대 30명까지 초대할 수 있습니다.") List<Long> userIds
    ) {}

    public record UpdateDepartureRequest(
        Long departurePlaceId,
        TransportType transportType
    ) {}

    public record ParticipantResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String status,
        boolean isHost
    ) {
        public static ParticipantResponse from(ParticipantInfo info) {
            return new ParticipantResponse(
                info.userId(),
                info.nickname(),
                info.profileImageUrl(),
                info.status().name(),
                info.isHost()
            );
        }
    }

    public record AppointmentResponse(
        Long id,
        String name,
        String placeName,
        String placeAddress,
        Double latitude,
        Double longitude,
        ZonedDateTime dateTime,
        String status,
        Long hostId,
        String transportType,
        Integer durationMinutes,
        ZonedDateTime departureAlertAt,
        String departurePlaceLabel,
        List<ParticipantResponse> participants
    ) {
        public static AppointmentResponse from(AppointmentInfo info) {
            List<ParticipantResponse> participants = info.participants().stream()
                .map(ParticipantResponse::from)
                .toList();
            return new AppointmentResponse(
                info.id(),
                info.name(),
                info.placeName(),
                info.placeAddress(),
                info.latitude(),
                info.longitude(),
                info.dateTime(),
                info.status().name(),
                info.hostId(),
                info.transportType() != null ? info.transportType().name() : null,
                info.durationMinutes(),
                info.departureAlertAt(),
                info.departurePlaceLabel(),
                participants
            );
        }
    }

    public record AppointmentDetailResponse(
        Long id,
        String name,
        String placeName,
        String placeAddress,
        Double latitude,
        Double longitude,
        ZonedDateTime dateTime,
        String status,
        Long hostId,
        String transportType,
        Integer durationMinutes,
        ZonedDateTime departureAlertAt,
        String departurePlaceLabel,
        List<ParticipantResponse> participants,
        boolean canNudge,
        Integer nudgeCooldownSeconds,
        boolean isHost,
        String myStatus
    ) {
        public static AppointmentDetailResponse from(AppointmentDetailInfo detail) {
            AppointmentResponse base = AppointmentResponse.from(detail.appointment());
            return new AppointmentDetailResponse(
                base.id(), base.name(), base.placeName(), base.placeAddress(),
                base.latitude(), base.longitude(), base.dateTime(), base.status(),
                base.hostId(), base.transportType(), base.durationMinutes(),
                base.departureAlertAt(), base.departurePlaceLabel(), base.participants(),
                detail.canNudge(),
                detail.nudgeCooldownSeconds(),
                detail.isHost(),
                detail.myStatus().name()
            );
        }
    }

    public record AppointmentListResponse(
        List<AppointmentResponse> appointments,
        boolean hasNext
    ) {
        public static AppointmentListResponse from(AppointmentListInfo info) {
            List<AppointmentResponse> responses = info.appointments().stream()
                .map(AppointmentResponse::from)
                .toList();
            return new AppointmentListResponse(responses, info.hasNext());
        }
    }

    public record CalendarAppointmentResponse(
        Long id,
        String name,
        ZonedDateTime dateTime,
        String placeName,
        String tagColor
    ) {
        public static CalendarAppointmentResponse from(CalendarAppointmentInfo info) {
            return new CalendarAppointmentResponse(
                info.id(), info.name(), info.dateTime(), info.placeName(), info.tagColor()
            );
        }
    }

    public record CalendarDayResponse(
        LocalDate date,
        List<CalendarAppointmentResponse> appointments
    ) {
        public static CalendarDayResponse from(CalendarDayInfo info) {
            List<CalendarAppointmentResponse> responses = info.appointments().stream()
                .map(CalendarAppointmentResponse::from)
                .toList();
            return new CalendarDayResponse(info.date(), responses);
        }
    }

    public record NudgeRequest(
        @NotNull @Size(max = 30, message = "최대 30명까지 콕 찌르기 가능합니다.") List<Long> targetUserIds
    ) {}

    public record DepartureUpdateResponse(
        Integer durationMinutes,
        ZonedDateTime departureAlertAt,
        String transportType,
        String departurePlaceLabel
    ) {
        public static DepartureUpdateResponse from(DepartureUpdateInfo info) {
            return new DepartureUpdateResponse(
                info.durationMinutes(),
                info.departureAlertAt(),
                info.transportType() != null ? info.transportType().name() : null,
                info.departurePlaceLabel()
            );
        }
    }
}
