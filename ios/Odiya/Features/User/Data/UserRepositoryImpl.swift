import Foundation

final class UserRepositoryImpl: UserRepository {

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getMyProfile() async throws -> UserResponseDTO {
        try await apiClient.request(
            endpoint: .getMyProfile,
            responseType: UserResponseDTO.self
        )
    }

    func updateNickname(_ nickname: String) async throws -> UserResponseDTO {
        let body = UpdateNicknameRequest(nickname: nickname)
        return try await apiClient.request(
            endpoint: .updateNickname,
            body: body,
            responseType: UserResponseDTO.self
        )
    }

    func updateProfileImage(_ imageUrl: String) async throws -> UserResponseDTO {
        let body = UpdateProfileImageRequest(profileImageUrl: imageUrl)
        return try await apiClient.request(
            endpoint: .updateProfileImage,
            body: body,
            responseType: UserResponseDTO.self
        )
    }

    func deleteAccount() async throws {
        try await apiClient.requestVoid(endpoint: .deleteAccount)
    }
}
