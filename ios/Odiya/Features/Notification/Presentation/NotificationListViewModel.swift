import Foundation

@MainActor
final class NotificationListViewModel: ObservableObject {

    @Published var notifications: [AppNotification] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var unreadCount: Int = 0

    private static let pageSize = 20

    private let repository: NotificationRepository
    private var cursor: Int64?
    private(set) var hasNext = false

    init(repository: NotificationRepository = NotificationRepositoryImpl()) {
        self.repository = repository
    }

    // MARK: - Load

    func loadNotifications() async {
        guard !isLoading else { return }
        await fetchNotifications(cursor: nil, reset: true)
    }

    func loadMoreIfNeeded() async {
        guard hasNext, !isLoading else { return }
        await fetchNotifications(cursor: cursor, reset: false)
    }

    private func fetchNotifications(cursor: Int64?, reset: Bool) async {
        isLoading = true
        if reset { errorMessage = nil }

        do {
            let response = try await repository.getNotifications(cursor: cursor, size: Self.pageSize)
            if reset {
                notifications = response.notifications.map { AppNotification(dto: $0) }
            } else {
                notifications += response.notifications.map { AppNotification(dto: $0) }
            }
            hasNext = response.hasNext
            self.cursor = response.nextCursor
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func loadUnreadCount() async {
        do {
            let response = try await repository.getUnreadCount()
            unreadCount = response.count
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

    // MARK: - Grouped Notifications

    var groupedNotifications: [(key: String, notifications: [AppNotification])] {
        let calendar = Calendar.current

        let grouped = Dictionary(grouping: notifications) { notification -> String in
            if calendar.isDateInToday(notification.createdAt) {
                return "오늘"
            } else if calendar.isDateInYesterday(notification.createdAt) {
                return "어제"
            } else {
                let month = calendar.component(.month, from: notification.createdAt)
                let day = calendar.component(.day, from: notification.createdAt)
                return "\(month)월 \(day)일"
            }
        }

        // Sort groups: 오늘 first, 어제 second, then by date descending
        let order: [String: Int] = ["오늘": 0, "어제": 1]
        return grouped
            .map { (key: $0.key, notifications: $0.value) }
            .sorted { lhs, rhs in
                let lOrder = order[lhs.key] ?? 2
                let rOrder = order[rhs.key] ?? 2
                if lOrder != rOrder { return lOrder < rOrder }
                // Both are date strings — compare by first notification's createdAt descending
                guard let lDate = lhs.notifications.first?.createdAt,
                      let rDate = rhs.notifications.first?.createdAt else { return false }
                return lDate > rDate
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
