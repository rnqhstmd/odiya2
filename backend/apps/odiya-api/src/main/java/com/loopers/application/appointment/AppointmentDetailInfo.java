package com.loopers.application.appointment;

import com.loopers.domain.appointment.ParticipantStatus;

public record AppointmentDetailInfo(
    AppointmentInfo appointment,
    boolean canNudge,
    Integer nudgeCooldownSeconds,
    boolean isHost,
    ParticipantStatus myStatus
) {}
