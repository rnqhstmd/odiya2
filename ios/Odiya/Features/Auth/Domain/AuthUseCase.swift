import Foundation

protocol AuthUseCaseProtocol {
    func loginWithKakao(accessToken: String) async throws
    func logout() async throws
}

final class AuthUseCase: AuthUseCaseProtocol {

    private let authRepository: AuthRepository
    private let tokenManager: TokenManager

    init(
        authRepository: AuthRepository = AuthRepositoryImpl(),
        tokenManager: TokenManager = .shared
    ) {
        self.authRepository = authRepository
        self.tokenManager = tokenManager
    }

    func loginWithKakao(accessToken: String) async throws {
        let response = try await authRepository.loginWithKakao(accessToken: accessToken)
        try await tokenManager.saveTokens(
            accessToken: response.accessToken,
            refreshToken: response.refreshToken
        )
    }

    func logout() async throws {
        if let refreshToken = await tokenManager.refreshToken {
            try? await authRepository.logout(refreshToken: refreshToken)
        }
        await tokenManager.clearTokens()
    }
}
