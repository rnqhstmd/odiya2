import Foundation

protocol UserRepository {
    func getMyProfile() async throws -> UserResponseDTO
    func updateNickname(_ nickname: String) async throws -> UserResponseDTO
    func updateProfileImage(_ imageUrl: String) async throws -> UserResponseDTO
    func deleteAccount() async throws
}
