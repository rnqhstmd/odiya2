package com.loopers.domain.auth;

import com.loopers.config.security.JwtProvider;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TokenService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public record TokenPair(String accessToken, String refreshToken) {}

    public TokenPair issueTokens(Long userId) {
        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId, tokenId);
        refreshTokenRepository.save(userId, tokenId, refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    public TokenPair rotate(String refreshToken) {
        JwtProvider.RefreshTokenClaims claims = jwtProvider.parseRefreshToken(refreshToken);
        Long userId = claims.userId();
        String tokenId = claims.tokenId();

        boolean deleted = refreshTokenRepository.deleteIfPresent(userId, tokenId);
        if (!deleted) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다.");
        }

        return issueTokens(userId);
    }

    public void invalidate(String refreshToken) {
        try {
            JwtProvider.RefreshTokenClaims claims = jwtProvider.parseRefreshToken(refreshToken);
            refreshTokenRepository.deleteIfPresent(claims.userId(), claims.tokenId());
        } catch (CoreException e) {
            // 만료되었거나 유효하지 않은 토큰으로 로그아웃 시도 시 무시 (UX: 로그아웃은 항상 성공)
        }
    }
}
