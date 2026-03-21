import SwiftUI

@MainActor
final class AddFriendViewModel: ObservableObject {

    @Published var searchText: String = ""
    @Published var pendingRequests: [FriendRequest] = []
    @Published var searchResults: [UserSearchResult] = []
    @Published var sentRequestIds: Set<Int64> = []
    @Published var showAlert: Bool = false
    @Published var alertMessage: String = ""
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Dependencies

    private let repository: FriendRepository

    init(repository: FriendRepository = FriendRepositoryImpl()) {
        self.repository = repository
    }

    // MARK: - Load

    func loadPendingRequests() async {
        isLoading = true
        defer { isLoading = false }
        do {
            let dtos = try await repository.getReceivedRequests()
            pendingRequests = dtos.map { FriendRequest(from: $0) }
        } catch {
            alertMessage = error.localizedDescription
            showAlert = true
        }
    }

    func searchUsers() async {
        guard !searchText.isEmpty else {
            searchResults = []
            return
        }
        do {
            let dtos = try await repository.searchUsers(nickname: searchText)
            searchResults = dtos.map { UserSearchResult(from: $0) }
        } catch {
            errorMessage = error.localizedDescription
            searchResults = []
        }
    }

    // MARK: - Actions

    func sendFriendRequest(to user: UserSearchResult) {
        Task {
            do {
                try await repository.sendFriendRequest(targetUserId: user.id)
                sentRequestIds.insert(user.id)
                alertMessage = "\(user.nickname)님에게 친구 요청을 보냈어요"
                showAlert = true
            } catch {
                alertMessage = error.localizedDescription
                showAlert = true
            }
        }
    }

    func acceptRequest(_ request: FriendRequest) {
        Task {
            do {
                try await repository.acceptFriendRequest(requestId: request.id)
                pendingRequests.removeAll { $0.id == request.id }
                alertMessage = "\(request.nickname)님과 친구가 되었어요!"
                showAlert = true
            } catch {
                alertMessage = error.localizedDescription
                showAlert = true
            }
        }
    }

    func rejectRequest(_ request: FriendRequest) {
        Task {
            do {
                try await repository.rejectFriendRequest(requestId: request.id)
                pendingRequests.removeAll { $0.id == request.id }
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
