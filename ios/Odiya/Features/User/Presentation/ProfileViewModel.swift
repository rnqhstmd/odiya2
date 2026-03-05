import Foundation

@MainActor
final class ProfileViewModel: ObservableObject {

    @Published var user: User?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var showDeleteConfirmation = false

    private let userUseCase: UserUseCaseProtocol
    private let authUseCase: AuthUseCaseProtocol
    var onLogout: (() -> Void)?

    init(
        userUseCase: UserUseCaseProtocol = UserUseCase(),
        authUseCase: AuthUseCaseProtocol = AuthUseCase()
    ) {
        self.userUseCase = userUseCase
        self.authUseCase = authUseCase
    }

    func loadProfile() {
        isLoading = true
        errorMessage = nil

        Task {
            do {
                user = try await userUseCase.getMyProfile()
            } catch let error as APIError {
                errorMessage = error.userMessage
            } catch {
                errorMessage = "프로필을 불러올 수 없습니다."
            }
            isLoading = false
        }
    }

    func updateNickname(_ nickname: String) {
        guard !nickname.isEmpty else { return }
        isLoading = true

        Task {
            do {
                user = try await userUseCase.updateNickname(nickname)
            } catch let error as APIError {
                errorMessage = error.userMessage
            } catch {
                errorMessage = "닉네임 변경에 실패했습니다."
            }
            isLoading = false
        }
    }

    func logout() {
        Task {
            try? await authUseCase.logout()
            onLogout?()
        }
    }

    func deleteAccount() {
        isLoading = true

        Task {
            do {
                try await userUseCase.deleteAccount()
                onLogout?()
            } catch let error as APIError {
                errorMessage = error.userMessage
            } catch {
                errorMessage = "회원 탈퇴에 실패했습니다."
            }
            isLoading = false
        }
    }
}
