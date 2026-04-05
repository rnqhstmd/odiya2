import Foundation

protocol UserUseCaseProtocol {
    func getMyProfile() async throws -> User
    func updateNickname(_ nickname: String) async throws -> User
    func updateProfileImage(_ imageUrl: String) async throws -> User
    func uploadAndUpdateProfileImage(_ imageData: Data) async throws -> User
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

    func uploadAndUpdateProfileImage(_ imageData: Data) async throws -> User {
        let fileName = "profile_\(UUID().uuidString).jpg"
        let contentType = "image/jpeg"

        // 1. Presigned URL 발급
        let presigned = try await userRepository.getPresignedURL(
            fileName: fileName,
            contentType: contentType
        )

        // 2. S3에 이미지 업로드
        try await ImageUploadService.shared.upload(
            imageData: imageData,
            to: presigned.presignedUrl,
            contentType: contentType
        )

        // 3. 프로필 이미지 URL 업데이트
        let dto = try await userRepository.updateProfileImage(presigned.imageUrl)
        return dto.toDomain()
    }

    func deleteAccount() async throws {
        try await userRepository.deleteAccount()
        await tokenManager.clearTokens()
    }
}
