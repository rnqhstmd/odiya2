package com.loopers.domain.traveltime;

import java.util.List;

public record TravelTimeCalculateEvent(List<Long> participantIds) {
}
