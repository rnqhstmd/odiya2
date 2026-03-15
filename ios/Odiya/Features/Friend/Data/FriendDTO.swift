import Foundation

// MARK: - Request Bodies

struct SendFriendRequestBody: Encodable {
    let targetUserId: Int64
}

struct ChangeFriendTagBody: Encodable {
    let tagId: Int64
}

struct CreateTagBody: Encodable {
    let name: String
    let color: String
}

struct UpdateTagBody: Encodable {
    let name: String?
    let color: String?
}

// MARK: - Response DTOs

/// 백엔드 FriendV1Dto.FriendResponse 매핑
struct FriendResponseDTO: Decodable {
    let friendUserId: Int64
    let nickname: String
    let profileImageUrl: String?
    let tag: TagResponseDTO?
    let status: String
}

/// 백엔드 FriendV1Dto.FriendRequestResponse 매핑
struct FriendRequestResponseDTO: Decodable {
    let requestId: Int64
    let fromUserId: Int64
    let nickname: String
    let profileImageUrl: String?
    let createdAt: String
}

/// 백엔드 UserV1Dto.UserSearchResponse 매핑
struct UserSearchResponseDTO: Decodable {
    let id: Int64
    let nickname: String
    let profileImageUrl: String?
    let friendStatus: String?
}

/// 백엔드 TagV1Dto.TagResponse 매핑
struct TagResponseDTO: Decodable {
    let id: Int64
    let name: String
    let color: String
    let isDefault: Bool
    let friendCount: Int
}
