import Foundation

// MARK: - Request DTOs

/// PATCH /api/v1/users/me/nickname
struct UpdateNicknameRequest: Encodable {
    let nickname: String
}

/// PATCH /api/v1/users/me/profile-image
struct UpdateProfileImageRequest: Encodable {
    let profileImageUrl: String
}

/// POST /api/v1/storage/presigned-url
struct PresignedURLRequest: Encodable {
    let fileName: String
    let contentType: String
}

struct PresignedURLResponse: Decodable {
    let presignedUrl: String
    let imageUrl: String
}

// MARK: - Response DTOs

/// 백엔드 UserV1Dto.UserResponse 매핑
struct UserResponseDTO: Decodable {
    let id: Int64
    let nickname: String
    let profileImageUrl: String?

    func toDomain() -> User {
        User(
            id: id,
            nickname: nickname,
            profileImageUrl: profileImageUrl
        )
    }
}
