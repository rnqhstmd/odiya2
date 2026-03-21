import Foundation

// MARK: - DeparturePlace DTOs

/// GET /api/v1/departure-places 응답 / POST·PATCH 응답
struct DeparturePlaceResponseDTO: Decodable {
    let id: Int64
    let label: String
    let address: String
    let latitude: Double
    let longitude: Double
}

/// POST /api/v1/departure-places
struct CreateDeparturePlaceRequest: Encodable {
    let label: String
    let address: String
    let latitude: Double
    let longitude: Double
}

/// PATCH /api/v1/departure-places/{id}
struct UpdateDeparturePlaceRequest: Encodable {
    let label: String?
    let address: String?
    let latitude: Double?
    let longitude: Double?
}

// MARK: - UserSettings DTOs

/// GET /api/v1/users/me/settings 응답
struct UserSettingsResponseDTO: Decodable {
    let defaultTransportType: TransportType
    let parkingBufferMinutes: Int
    let extraMinutes: Int
}

/// PATCH /api/v1/users/me/settings
struct UpdateUserSettingsRequest: Encodable {
    let defaultTransportType: TransportType?
    let parkingBufferMinutes: Int?
    let extraMinutes: Int?
}

// MARK: - Tag DTOs

// TagResponseDTO는 FriendDTO.swift에 정의되어 있음

/// POST /api/v1/tags
struct CreateTagRequest: Encodable {
    let name: String
    let color: String
}

/// PATCH /api/v1/tags/{id}
struct UpdateTagRequest: Encodable {
    let name: String?
    let color: String?
}
