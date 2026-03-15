package com.loopers.domain.notification;

import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public DeviceToken register(User user, String token, DeviceType deviceType) {
        Optional<DeviceToken> existing = deviceTokenRepository.findByToken(token);
        if (existing.isPresent()) {
            DeviceToken deviceToken = existing.get();
            deviceToken.reactivate(user);
            return deviceTokenRepository.save(deviceToken);
        }
        DeviceToken deviceToken = DeviceToken.create(user, token, deviceType);
        return deviceTokenRepository.save(deviceToken);
    }

    public void deactivate(String token, Long userId) {
        deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
            if (deviceToken.isOwnedBy(userId)) {
                deviceToken.deactivate();
                deviceTokenRepository.save(deviceToken);
            }
        });
    }

    public void deactivateByToken(String token) {
        deviceTokenRepository.findByToken(token).ifPresent(deviceToken -> {
            deviceToken.deactivate();
            deviceTokenRepository.save(deviceToken);
        });
    }

    public List<DeviceToken> getActiveTokens(Long userId) {
        return deviceTokenRepository.findActiveByUserId(userId);
    }

    public List<String> getActiveTokenStrings(Long userId) {
        return deviceTokenRepository.findActiveByUserId(userId).stream()
            .map(DeviceToken::getToken)
            .toList();
    }
}
