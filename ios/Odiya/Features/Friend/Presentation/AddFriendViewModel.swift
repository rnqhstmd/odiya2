import SwiftUI

@MainActor
final class AddFriendViewModel: ObservableObject {

    @Published var searchText: String = ""
    @Published var pendingRequests: [Friend] = MockData.pendingFriends
    @Published var searchResults: [Friend] = []
    @Published var sentRequestIds: Set<Int64> = []
    @Published var showAlert: Bool = false
    @Published var alertMessage: String = ""

    // Mock 검색 결과 (오디야 사용자)
    private let mockUsers: [Friend] = [
        Friend(id: 201, nickname: "임지수", status: .accepted),
        Friend(id: 202, nickname: "오태양", status: .accepted),
        Friend(id: 203, nickname: "한소희", status: .accepted),
    ]

    var filteredSearchResults: [Friend] {
        guard !searchText.isEmpty else { return [] }
        return mockUsers.filter {
            $0.nickname.localizedCaseInsensitiveContains(searchText)
        }
    }

    func sendFriendRequest(to friend: Friend) {
        sentRequestIds.insert(friend.id)
        alertMessage = "\(friend.nickname)님에게 친구 요청을 보냈어요"
        showAlert = true
    }

    func acceptRequest(_ friend: Friend) {
        pendingRequests.removeAll { $0.id == friend.id }
        alertMessage = "\(friend.nickname)님과 친구가 되었어요!"
        showAlert = true
    }

    func rejectRequest(_ friend: Friend) {
        pendingRequests.removeAll { $0.id == friend.id }
    }
}
