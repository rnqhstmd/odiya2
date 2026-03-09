package com.loopers.application.appointment;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayInfo(
    LocalDate date,
    List<CalendarAppointmentInfo> appointments
) {}
