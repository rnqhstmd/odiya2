package com.loopers.infrastructure.usersettings;

import com.loopers.domain.usersettings.UserSettings;
import com.loopers.domain.usersettings.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserSettingsRepositoryImpl implements UserSettingsRepository {

    private final UserSettingsJpaRepository userSettingsJpaRepository;

    @Override
    public UserSettings save(UserSettings userSettings) {
        return userSettingsJpaRepository.save(userSettings);
    }

    @Override
    public Optional<UserSettings> findActiveByUserId(Long userId) {
        return userSettingsJpaRepository.findActiveByUserId(userId);
    }
}
