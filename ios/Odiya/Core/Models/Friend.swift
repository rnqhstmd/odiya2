import Foundation

enum FriendshipStatus: String, Codable, CaseIterable {
    case pending  = "PENDING"
    case accepted = "ACCEPTED"

    var displayName: String {
        switch self {
        case .pending:  return "요청 중"
        case .accepted: return "친구"
        }
    }
}

struct Friend: Identifiable, Equatable, Hashable {
    let id: Int64
    var nickname: String
    var profileImageUrl: String?
    var tag: Tag
    var status: FriendshipStatus

    init(
        id: Int64,
        nickname: String,
        profileImageUrl: String? = nil,
        tag: Tag = .friend,
        status: FriendshipStatus = .accepted
    ) {
        self.id = id
        self.nickname = nickname
        self.profileImageUrl = profileImageUrl
        self.tag = tag
        self.status = status
    }

    // MARK: - DTO 변환

    init(from dto: FriendResponseDTO) {
        self.id = dto.friendUserId
        self.nickname = dto.nickname
        self.profileImageUrl = dto.profileImageUrl
        self.tag = dto.tag.map { Tag(from: $0) } ?? .friend
        self.status = FriendshipStatus(rawValue: dto.status) ?? .accepted
    }
}

// MARK: - 사용자 검색 결과 모델

struct UserSearchResult: Identifiable {
    let id: Int64
    let nickname: String
    let profileImageUrl: String?
    let friendStatus: String?

    init(from dto: UserSearchResponseDTO) {
        self.id = dto.id
        self.nickname = dto.nickname
        self.profileImageUrl = dto.profileImageUrl
        self.friendStatus = dto.friendStatus
    }
}

// MARK: - 받은 친구 요청 모델

struct FriendRequest: Identifiable {
    let id: Int64
    let fromUserId: Int64
    let nickname: String
    let profileImageUrl: String?

    init(from dto: FriendRequestResponseDTO) {
        self.id = dto.requestId
        self.fromUserId = dto.fromUserId
        self.nickname = dto.nickname
        self.profileImageUrl = dto.profileImageUrl
    }
}
