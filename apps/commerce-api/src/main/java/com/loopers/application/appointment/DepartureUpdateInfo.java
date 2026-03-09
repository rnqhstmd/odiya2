package com.loopers.application.appointment;

import com.loopers.domain.usersettings.TransportType;

import java.time.ZonedDateTime;

public record DepartureUpdateInfo(
    Integer durationMinutes,
    ZonedDateTime departureAlertAt,
    TransportType transportType,
    String departurePlaceLabel
) {}
