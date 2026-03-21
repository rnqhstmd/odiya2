package com.loopers.application.usersettings;

import com.loopers.domain.usersettings.TransportType;
import com.loopers.domain.usersettings.UserSettings;

public record UserSettingsInfo(TransportType defaultTransportType,
                               Integer parkingBufferMinutes,
                               Integer extraMinutes) {
    public static UserSettingsInfo from(UserSettings settings) {
        return new UserSettingsInfo(
            settings.getDefaultTransportType(),
            settings.getParkingBufferMinutes(),
            settings.getExtraMinutes()
        );
    }
}
