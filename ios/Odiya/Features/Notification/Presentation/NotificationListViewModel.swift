import Foundation

@MainActor
final class NotificationListViewModel: ObservableObject {

    @Published var notifications: [AppNotification] = []
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var unreadCount: Int = 0

    private static let pageSize = 20

    private let repository: NotificationRepository
    private let friendRepository: FriendRepository
    private var cursor: Int64?
    private(set) var hasNext = false

    init(
        repository: NotificationRepository = NotificationRepositoryImpl(),
        friendRepository: FriendRepository = FriendRepositoryImpl()
    ) {
        self.repository = repository
        self.friendRepository = friendRepository
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

        // 날짜의 시작(startOfDay)을 그룹 키로 사용하여 연도 간 충돌 방지
        let grouped = Dictionary(grouping: notifications) { notification -> Date in
            calendar.startOfDay(for: notification.createdAt)
        }

        return grouped
            .map { date, items -> (key: String, notifications: [AppNotification]) in
                let sortedItems = items.sorted { $0.createdAt > $1.createdAt }
                let label: String
                if calendar.isDateInToday(date) {
                    label = "오늘"
                } else if calendar.isDateInYesterday(date) {
                    label = "어제"
                } else {
                    let year = calendar.component(.year, from: date)
                    let month = calendar.component(.month, from: date)
                    let day = calendar.component(.day, from: date)
                    let currentYear = calendar.component(.year, from: Date())
                    label = year == currentYear ? "\(month)월 \(day)일" : "\(year)년 \(month)월 \(day)일"
                }
                return (key: label, notifications: sortedItems)
            }
            .sorted { lhs, rhs in
                let lDate = lhs.notifications.first?.createdAt ?? .distantPast
                let rDate = rhs.notifications.first?.createdAt ?? .distantPast
                return lDate > rDate
            }
    }

    func acceptFriendRequest(notification: AppNotification) {
        guard let requestId = notification.referenceId else { return }
        Task {
            do {
                try await friendRepository.acceptFriendRequest(requestId: requestId)
                await markAsRead(id: notification.id)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }

    func declineFriendRequest(notification: AppNotification) {
        guard let requestId = notification.referenceId else { return }
        Task {
            do {
                try await friendRepository.rejectFriendRequest(requestId: requestId)
                await markAsRead(id: notification.id)
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
