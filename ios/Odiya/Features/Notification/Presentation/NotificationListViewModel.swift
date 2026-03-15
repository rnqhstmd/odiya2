import Foundation

@MainActor
final class NotificationListViewModel: ObservableObject {

    @Published var notifications: [AppNotification] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let repository: NotificationRepository
    private var cursor: Int64?
    private(set) var hasNext = false

    init(repository: NotificationRepository = NotificationRepositoryImpl()) {
        self.repository = repository
    }

    // MARK: - Computed

    var unreadCount: Int {
        notifications.filter { !$0.isRead }.count
    }

    // MARK: - Load

    func loadNotifications() async {
        guard !isLoading else { return }
        isLoading = true
        errorMessage = nil
        cursor = nil

        do {
            let response = try await repository.getNotifications(cursor: nil, size: 20)
            notifications = response.notifications.map { AppNotification(dto: $0) }
            hasNext = response.hasNext
            cursor = response.nextCursor
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadMoreIfNeeded() async {
        guard hasNext, !isLoading else { return }
        isLoading = true

        do {
            let response = try await repository.getNotifications(cursor: cursor, size: 20)
            notifications += response.notifications.map { AppNotification(dto: $0) }
            hasNext = response.hasNext
            cursor = response.nextCursor
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadUnreadCount() async {
        do {
            let response = try await repository.getUnreadCount()
            _ = response.count
        } catch {
            // unread count failure is non-critical
        }
    }

    // MARK: - Actions

    func markAsRead(id: String) async {
        guard let numericId = Int64(id) else { return }
        guard let index = notifications.firstIndex(where: { $0.id == id }) else { return }
        notifications[index].isRead = true

        do {
            try await repository.markAsRead(id: numericId)
        } catch {
            // revert optimistic update
            notifications[index].isRead = false
            errorMessage = error.localizedDescription
        }
    }

    func markAllAsRead() async {
        let previous = notifications
        for index in notifications.indices {
            notifications[index].isRead = true
        }

        do {
            _ = try await repository.markAllAsRead()
        } catch {
            // revert optimistic update
            notifications = previous
            errorMessage = error.localizedDescription
        }
    }

    func acceptFriendRequest(id: String) {
        Task { await markAsRead(id: id) }
        // TODO: 친구 요청 수락 API 연동
    }

    func declineFriendRequest(id: String) {
        Task { await markAsRead(id: id) }
        // TODO: 친구 요청 거절 API 연동
    }
}
