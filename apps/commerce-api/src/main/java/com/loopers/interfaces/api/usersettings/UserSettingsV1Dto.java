package com.loopers.interfaces.api.usersettings;

import com.loopers.application.usersettings.UserSettingsInfo;
import com.loopers.domain.usersettings.TransportType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UserSettingsV1Dto {

    public record UpdateUserSettingsRequest(
        TransportType defaultTransportType,
        @Min(0) @Max(60) Integer parkingBufferMinutes,
        @Min(0) @Max(60) Integer extraMinutes
    ) {}

    public record UserSettingsResponse(
        TransportType defaultTransportType,
        Integer parkingBufferMinutes,
        Integer extraMinutes
    ) {
        public static UserSettingsResponse from(UserSettingsInfo info) {
            return new UserSettingsResponse(
                info.defaultTransportType(),
                info.parkingBufferMinutes(),
                info.extraMinutes()
            );
        }
    }
}
