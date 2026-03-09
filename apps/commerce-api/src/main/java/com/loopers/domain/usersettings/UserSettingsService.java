package com.loopers.domain.usersettings;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserService userService;

    @Transactional
    public UserSettings getOrCreateByUserId(Long userId) {
        return userSettingsRepository.findActiveByUserIdWithLock(userId)
            .orElseGet(() -> {
                User user = userService.getUser(userId);
                UserSettings settings = UserSettings.createDefault(user);
                return userSettingsRepository.save(settings);
            });
    }

    @Transactional
    public UserSettings update(Long userId, TransportType defaultTransportType,
                               Integer parkingBufferMinutes, Integer extraMinutes) {
        UserSettings settings = getOrCreateByUserId(userId);
        settings.update(defaultTransportType, parkingBufferMinutes, extraMinutes);
        return settings;
    }
}
