package com.loopers.domain.auth;

import java.util.Optional;

public interface RefreshTokenRepository {
    void save(Long userId, String tokenId, String refreshToken);
    Optional<String> find(Long userId, String tokenId);
    boolean deleteIfPresent(Long userId, String tokenId);
}
