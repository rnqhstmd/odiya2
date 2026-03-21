package com.loopers.infrastructure.notification;

import com.loopers.domain.notification.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenJpaRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByToken(String token);
    List<DeviceToken> findByUserIdAndActiveTrue(Long userId);
}
