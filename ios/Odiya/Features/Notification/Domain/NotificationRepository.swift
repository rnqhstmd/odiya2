import Foundation

protocol NotificationRepository {
    func getNotifications(cursor: Int64?, size: Int) async throws -> NotificationListResponseDTO
    func markAsRead(id: Int64) async throws
    func markAllAsRead() async throws -> ReadAllResponseDTO
    func getUnreadCount() async throws -> UnreadCountResponseDTO
    func registerDevice(token: String) async throws
    func deactivateDevice(token: String) async throws
}
