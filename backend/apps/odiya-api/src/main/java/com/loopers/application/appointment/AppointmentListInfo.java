package com.loopers.application.appointment;

import java.util.List;

public record AppointmentListInfo(
    List<AppointmentInfo> appointments,
    boolean hasNext
) {}
