import Foundation

// MARK: - Request DTOs

/// POST /api/v1/auth/kakao/login
struct KakaoLoginRequest: Encodable {
    let kakaoAccessToken: String
}

/// POST /api/v1/auth/token/refresh
struct RefreshTokenRequest: Encodable {
    let refreshToken: String
}

/// POST /api/v1/auth/logout
struct LogoutRequest: Encodable {
    let refreshToken: String
}

// MARK: - Response DTOs

/// 백엔드 AuthV1Dto.TokenResponse 매핑
struct TokenResponseDTO: Decodable {
    let accessToken: String
    let refreshToken: String
}
