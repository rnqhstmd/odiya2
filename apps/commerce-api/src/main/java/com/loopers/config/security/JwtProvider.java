package com.loopers.config.security;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";
    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_TOKEN_ID = "tid";

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtProvider(JwtProperties jwtProperties) {
        String secret = jwtProperties.secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT 시크릿 키는 최소 32바이트 이상이어야 합니다.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiryMs = jwtProperties.accessTokenExpirySeconds() * 1000L;
        this.refreshTokenExpiryMs = jwtProperties.refreshTokenExpirySeconds() * 1000L;
    }

    public String createAccessToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim(CLAIM_TYPE, TYPE_ACCESS)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessTokenExpiryMs))
            .signWith(secretKey)
            .compact();
    }

    public String createRefreshToken(Long userId, String tokenId) {
        Date now = new Date();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim(CLAIM_TYPE, TYPE_REFRESH)
            .claim(CLAIM_TOKEN_ID, tokenId)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + refreshTokenExpiryMs))
            .signWith(secretKey)
            .compact();
    }

    public LoginUser parseAccessToken(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get(CLAIM_TYPE, String.class);
        if (!TYPE_ACCESS.equals(type)) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "액세스 토큰이 아닙니다.");
        }
        Long userId = Long.valueOf(claims.getSubject());
        return new LoginUser(userId);
    }

    public RefreshTokenClaims parseRefreshToken(String token) {
        Claims claims = parseClaims(token);
        String type = claims.get(CLAIM_TYPE, String.class);
        if (!TYPE_REFRESH.equals(type)) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "리프레시 토큰이 아닙니다.");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String tokenId = claims.get(CLAIM_TOKEN_ID, String.class);
        return new RefreshTokenClaims(userId, tokenId);
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        } catch (ExpiredJwtException e) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "만료된 토큰입니다.");
        } catch (JwtException | IllegalArgumentException e) {
            throw new CoreException(ErrorType.UNAUTHORIZED, "유효하지 않은 토큰입니다.");
        }
    }

    public record RefreshTokenClaims(Long userId, String tokenId) {}
}
