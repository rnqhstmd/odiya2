import Foundation
import KakaoSDKUser
import KakaoSDKAuth

@MainActor
final class LoginViewModel: ObservableObject {

    @Published var isLoading = false
    @Published var errorMessage: String?
    var onLoginSuccess: (() -> Void)?

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
                isLoading = false
                onLoginSuccess?()
            } catch {
                if let error = error as? APIError {
                    errorMessage = error.userMessage
                } else {
                    errorMessage = "로그인에 실패했습니다."
                }
                isLoading = false
            }
        }
    }

    private func getKakaoToken() async throws -> OAuthToken {
        return try await withCheckedThrowingContinuation { continuation in
            let completion: (OAuthToken?, Error?) -> Void = { oauthToken, error in
                if let error = error {
                    continuation.resume(throwing: error)
                } else if let oauthToken = oauthToken {
                    continuation.resume(returning: oauthToken)
                } else {
                    continuation.resume(throwing: APIError.unknown("카카오 로그인 응답을 받지 못했습니다."))
                }
            }

            if UserApi.isKakaoTalkLoginAvailable() {
                UserApi.shared.loginWithKakaoTalk(completionHandler: completion)
            } else {
                UserApi.shared.loginWithKakaoAccount(completionHandler: completion)
            }
        }
    }
}
