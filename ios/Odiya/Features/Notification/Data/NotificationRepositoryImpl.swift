import Foundation

final class NotificationRepositoryImpl: NotificationRepository {

    private static let iosDeviceType = "IOS"

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getNotifications(cursor: Int64?, size: Int) async throws -> NotificationListResponseDTO {
        return try await apiClient.request(
            endpoint: .getNotifications(cursor: cursor, size: size),
            responseType: NotificationListResponseDTO.self
        )
    }

    func markAsRead(id: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .markNotificationAsRead(id: id))
    }

    func markAllAsRead() async throws -> ReadAllResponseDTO {
        return try await apiClient.request(
            endpoint: .markAllNotificationsAsRead,
            responseType: ReadAllResponseDTO.self
        )
    }

    func getUnreadCount() async throws -> UnreadCountResponseDTO {
        return try await apiClient.request(
            endpoint: .getUnreadCount,
            responseType: UnreadCountResponseDTO.self
        )
    }

    func registerDevice(token: String) async throws {
        let body = RegisterDeviceRequest(token: token, deviceType: Self.iosDeviceType)
        try await apiClient.requestVoid(endpoint: .registerDevice, body: body)
    }

    func deactivateDevice(token: String) async throws {
        try await apiClient.requestVoid(endpoint: .deactivateDevice(token: token))
    }
}
