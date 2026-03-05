import Foundation

final class AuthRepositoryImpl: AuthRepository {

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func loginWithKakao(accessToken: String) async throws -> TokenResponseDTO {
        let body = KakaoLoginRequest(kakaoAccessToken: accessToken)
        return try await apiClient.request(
            endpoint: .kakaoLogin,
            body: body,
            responseType: TokenResponseDTO.self
        )
    }

    func refreshToken(_ refreshToken: String) async throws -> TokenResponseDTO {
        let body = RefreshTokenRequest(refreshToken: refreshToken)
        return try await apiClient.request(
            endpoint: .refreshToken,
            body: body,
            responseType: TokenResponseDTO.self
        )
    }

    func logout(refreshToken: String) async throws {
        let body = LogoutRequest(refreshToken: refreshToken)
        try await apiClient.requestVoid(endpoint: .logout, body: body)
    }
}
