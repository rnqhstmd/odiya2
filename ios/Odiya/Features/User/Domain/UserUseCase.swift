import Foundation

protocol UserUseCaseProtocol {
    func getMyProfile() async throws -> User
    func updateNickname(_ nickname: String) async throws -> User
    func updateProfileImage(_ imageUrl: String) async throws -> User
    func deleteAccount() async throws
}

final class UserUseCase: UserUseCaseProtocol {

    private let userRepository: UserRepository
    private let tokenManager: TokenManager

    init(
        userRepository: UserRepository = UserRepositoryImpl(),
        tokenManager: TokenManager = .shared
    ) {
        self.userRepository = userRepository
        self.tokenManager = tokenManager
    }

    func getMyProfile() async throws -> User {
        let dto = try await userRepository.getMyProfile()
        return dto.toDomain()
    }

    func updateNickname(_ nickname: String) async throws -> User {
        let dto = try await userRepository.updateNickname(nickname)
        return dto.toDomain()
    }

    func updateProfileImage(_ imageUrl: String) async throws -> User {
        let dto = try await userRepository.updateProfileImage(imageUrl)
        return dto.toDomain()
    }

    func deleteAccount() async throws {
        try await userRepository.deleteAccount()
        await tokenManager.clearTokens()
    }
}
