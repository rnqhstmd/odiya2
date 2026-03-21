import Foundation

protocol AuthRepository {
    func loginWithKakao(accessToken: String) async throws -> TokenResponseDTO
    func refreshToken(_ refreshToken: String) async throws -> TokenResponseDTO
    func logout(refreshToken: String) async throws
}
