package com.loopers.interfaces.api.usersettings;

import com.loopers.application.usersettings.UserSettingsInfo;
import com.loopers.domain.usersettings.TransportType;

public class UserSettingsV1Dto {

    public record UpdateUserSettingsRequest(
        TransportType defaultTransportType,
        Integer parkingBufferMinutes,
        Integer extraMinutes
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
