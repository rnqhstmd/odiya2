import Foundation

/// 도메인 엔티티
struct User: Identifiable, Equatable {
    let id: Int64
    var nickname: String
    var profileImageUrl: String?
}
