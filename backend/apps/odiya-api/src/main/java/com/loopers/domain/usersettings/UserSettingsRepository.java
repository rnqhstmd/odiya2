package com.loopers.domain.usersettings;

import java.util.Optional;

public interface UserSettingsRepository {
    UserSettings save(UserSettings userSettings);
    Optional<UserSettings> findActiveByUserId(Long userId);
    Optional<UserSettings> findActiveByUserIdWithLock(Long userId);
}
