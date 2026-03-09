package com.loopers.application.appointment;

import com.loopers.domain.appointment.AppointmentParticipant;
import com.loopers.domain.appointment.ParticipantStatus;
import com.loopers.domain.user.User;

public record ParticipantInfo(
    Long userId,
    String nickname,
    String profileImageUrl,
    ParticipantStatus status,
    boolean isHost
) {
    public static ParticipantInfo from(AppointmentParticipant participant, Long hostId) {
        User user = participant.getUser();
        return new ParticipantInfo(
            user.getId(),
            user.getNickname(),
            user.getProfileImageUrl(),
            participant.getStatus(),
            user.getId().equals(hostId)
        );
    }
}
