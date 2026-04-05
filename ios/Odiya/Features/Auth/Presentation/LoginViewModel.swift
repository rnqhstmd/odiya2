import Foundation
import KakaoSDKUser
import KakaoSDKAuth

@MainActor
final class LoginViewModel: ObservableObject {

    @Published var isLoading = false
    @Published var errorMessage: String?

    private let authUseCase: AuthUseCaseProtocol

    init(authUseCase: AuthUseCaseProtocol = AuthUseCase()) {
        self.authUseCase = authUseCase
    }

    func loginWithKakao() {
        isLoading = true
        errorMessage = nil

        Task {
            do {
                let oauthToken = try await getKakaoToken()
                try await authUseCase.loginWithKakao(accessToken: oauthToken.accessToken)
            } catch let error as APIError {
                errorMessage = error.userMessage
            } catch {
                errorMessage = "로그인에 실패했습니다."
            }
            isLoading = false
        }
    }

    private func getKakaoToken() async throws -> OAuthToken {
        return try await withCheckedThrowingContinuation { continuation in
            if UserApi.isKakaoTalkLoginAvailable() {
                UserApi.shared.loginWithKakaoTalk { oauthToken, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                    } else if let oauthToken = oauthToken {
                        continuation.resume(returning: oauthToken)
                    }
                }
            } else {
                UserApi.shared.loginWithKakaoAccount { oauthToken, error in
                    if let error = error {
                        continuation.resume(throwing: error)
                    } else if let oauthToken = oauthToken {
                        continuation.resume(returning: oauthToken)
                    }
                }
            }
        }
    }
}
