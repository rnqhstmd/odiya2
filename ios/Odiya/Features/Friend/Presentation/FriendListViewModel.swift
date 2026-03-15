import SwiftUI

@MainActor
final class FriendListViewModel: ObservableObject {

    // MARK: - Published

    @Published var friends: [Friend] = []
    @Published var searchText: String = ""
    @Published var selectedTag: Tag? = nil
    @Published var showEndFriendshipAlert: Bool = false
    @Published var endFriendshipTarget: Friend? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Dependencies

    private let repository: FriendRepository

    init(repository: FriendRepository = FriendRepositoryImpl()) {
        self.repository = repository
    }

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

    // MARK: - Load

    func loadFriends() async {
        isLoading = true
        errorMessage = nil
        do {
            let dtos = try await repository.getFriends(tagId: selectedTag.flatMap { Int64($0.id) })
            friends = dtos.map { Friend(from: $0) }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    // MARK: - Actions

    func confirmEndFriendship(_ friend: Friend) {
        endFriendshipTarget = friend
        showEndFriendshipAlert = true
    }

    func endFriendship() {
        guard let target = endFriendshipTarget else { return }
        Task {
            do {
                try await repository.removeFriend(friendUserId: target.id)
                friends.removeAll { $0.id == target.id }
            } catch {
                errorMessage = error.localizedDescription
            }
            endFriendshipTarget = nil
        }
    }

    func changeTag(friendId: Int64, tag: Tag) {
        guard let tagId = Int64(tag.id) else { return }
        Task {
            do {
                try await repository.changeFriendTag(friendUserId: friendId, tagId: tagId)
                if let index = friends.firstIndex(where: { $0.id == friendId }) {
                    friends[index].tag = tag
                }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
