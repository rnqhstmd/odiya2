import SwiftUI

@MainActor
final class FriendListViewModel: ObservableObject {

    // MARK: - Published

    @Published var friends: [Friend] = MockData.friends
    @Published var searchText: String = ""
    @Published var selectedTag: Tag? = nil
    @Published var showEndFriendshipAlert: Bool = false
    @Published var endFriendshipTarget: Friend? = nil

    // MARK: - Computed

    var filteredFriends: [Friend] {
        friends.filter { friend in
            guard friend.status == .accepted else { return false }
            let matchesTag = selectedTag == nil || friend.tag == selectedTag
            let matchesSearch = searchText.isEmpty ||
                friend.nickname.localizedCaseInsensitiveContains(searchText)
            return matchesTag && matchesSearch
        }
    }

    var availableTags: [Tag] {
        var seen = Set<Tag>()
        return friends.filter { $0.status == .accepted }.compactMap { friend in
            guard !seen.contains(friend.tag) else { return nil }
            seen.insert(friend.tag)
            return friend.tag
        }
    }

    // MARK: - Actions

    func confirmEndFriendship(_ friend: Friend) {
        endFriendshipTarget = friend
        showEndFriendshipAlert = true
    }

    func endFriendship() {
        guard let target = endFriendshipTarget else { return }
        friends.removeAll { $0.id == target.id }
        endFriendshipTarget = nil
    }

    func changeTag(friendId: Int64, tag: Tag) {
        if let index = friends.firstIndex(where: { $0.id == friendId }) {
            friends[index].tag = tag
        }
    }
}
