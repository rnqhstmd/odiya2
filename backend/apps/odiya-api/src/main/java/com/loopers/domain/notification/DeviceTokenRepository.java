package com.loopers.domain.notification;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository {
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findActiveByUserId(Long userId);
    DeviceToken save(DeviceToken deviceToken);
}
