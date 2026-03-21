package com.loopers.application.appointment;

import java.time.ZonedDateTime;

public record CalendarAppointmentInfo(
    Long id,
    String name,
    ZonedDateTime dateTime,
    String placeName,
    String tagColor
) {}
