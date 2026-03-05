import Foundation

final class NotificationListViewModel: ObservableObject {

    @Published var notifications: [AppNotification] = MockData.notifications

    // MARK: - Computed

    var unreadCount: Int {
        notifications.filter { !$0.isRead }.count
    }

    // MARK: - Actions

    func markAsRead(id: String) {
        guard let index = notifications.firstIndex(where: { $0.id == id }) else { return }
        notifications[index].isRead = true
    }

    func markAllAsRead() {
        for index in notifications.indices {
            notifications[index].isRead = true
        }
    }

    func acceptFriendRequest(id: String) {
        markAsRead(id: id)
        // TODO: 친구 요청 수락 API 연동
    }

    func declineFriendRequest(id: String) {
        markAsRead(id: id)
        // TODO: 친구 요청 거절 API 연동
    }
}
