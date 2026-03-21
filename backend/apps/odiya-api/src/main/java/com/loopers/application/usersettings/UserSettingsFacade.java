package com.loopers.application.usersettings;

import com.loopers.domain.usersettings.TransportType;
import com.loopers.domain.usersettings.UserSettings;
import com.loopers.domain.usersettings.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserSettingsFacade {

    private final UserSettingsService userSettingsService;

    public UserSettingsInfo getMySettings(Long userId) {
        UserSettings settings = userSettingsService.getOrCreateByUserId(userId);
        return UserSettingsInfo.from(settings);
    }

    public UserSettingsInfo updateMySettings(Long userId, TransportType defaultTransportType,
                                             Integer parkingBufferMinutes, Integer extraMinutes) {
        UserSettings settings = userSettingsService.update(userId, defaultTransportType,
            parkingBufferMinutes, extraMinutes);
        return UserSettingsInfo.from(settings);
    }
}
