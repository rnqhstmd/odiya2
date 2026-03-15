import Foundation

// MARK: - Response DTOs

/// 백엔드 NotificationV1Dto.NotificationResponse 매핑
struct NotificationResponseDTO: Decodable {
    let id: Int64
    let type: String
    let title: String
    let body: String
    let referenceId: Int64?
    let referenceType: String?
    let isRead: Bool
    let createdAt: String
}

/// 백엔드 NotificationV1Dto.NotificationListResponse 매핑
struct NotificationListResponseDTO: Decodable {
    let notifications: [NotificationResponseDTO]
    let hasNext: Bool
    let nextCursor: Int64?
}

/// 백엔드 NotificationV1Dto.ReadAllResponse 매핑
struct ReadAllResponseDTO: Decodable {
    let count: Int
}

/// 백엔드 NotificationV1Dto.UnreadCountResponse 매핑
struct UnreadCountResponseDTO: Decodable {
    let count: Int
}

// MARK: - Request DTOs

/// POST /api/v1/devices
struct RegisterDeviceRequest: Encodable {
    let token: String
    let deviceType: String
}
