package com.loopers.infrastructure.notification;

import com.loopers.domain.notification.DeviceToken;
import com.loopers.domain.notification.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class DeviceTokenRepositoryImpl implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository deviceTokenJpaRepository;

    @Override
    public Optional<DeviceToken> findByToken(String token) {
        return deviceTokenJpaRepository.findByToken(token);
    }

    @Override
    public List<DeviceToken> findActiveByUserId(Long userId) {
        return deviceTokenJpaRepository.findByUserIdAndActiveTrue(userId);
    }

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        return deviceTokenJpaRepository.save(deviceToken);
    }
}
