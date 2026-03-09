package com.loopers.application.appointment;

import com.loopers.domain.appointment.Appointment;
import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.AppointmentStatus;
import com.loopers.domain.usersettings.TransportType;

import java.time.ZonedDateTime;
import java.util.List;

public record AppointmentInfo(
    Long id,
    String name,
    String placeName,
    String placeAddress,
    Double latitude,
    Double longitude,
    ZonedDateTime dateTime,
    AppointmentStatus status,
    Long hostId,
    TransportType transportType,
    Integer durationMinutes,
    ZonedDateTime departureAlertAt,
    String departurePlaceLabel,
    List<ParticipantInfo> participants
) {
    public static AppointmentInfo from(Appointment appointment, Long currentUserId) {
        Long hostId = appointment.getHost().getId();

        AppointmentParticipant currentParticipant = appointment.getParticipants().stream()
            .filter(p -> p.getUser().getId().equals(currentUserId))
            .findFirst()
            .orElse(null);

        TransportType transportType = currentParticipant != null
            ? currentParticipant.getTransportType() : null;
        String departurePlaceLabel = currentParticipant != null
            && currentParticipant.getDeparturePlace() != null
            ? currentParticipant.getDeparturePlace().getLabel() : null;

        List<ParticipantInfo> participantInfos = appointment.getParticipants().stream()
            .map(p -> ParticipantInfo.from(p, hostId))
            .toList();

        return new AppointmentInfo(
            appointment.getId(),
            appointment.getName(),
            appointment.getPlaceName(),
            appointment.getPlaceAddress(),
            appointment.getLatitude(),
            appointment.getLongitude(),
            appointment.getDateTime(),
            appointment.getStatus(),
            hostId,
            transportType,
            null,  // durationMinutes - Phase 4
            null,  // departureAlertAt - Phase 4
            departurePlaceLabel,
            participantInfos
        );
    }
}
