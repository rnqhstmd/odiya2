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
}
