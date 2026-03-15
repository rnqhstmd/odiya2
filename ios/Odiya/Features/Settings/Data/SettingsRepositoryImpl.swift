import Foundation

final class SettingsRepositoryImpl: SettingsRepository {

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    // MARK: - DeparturePlace

    func getDeparturePlaces() async throws -> [DeparturePlaceResponseDTO] {
        return try await apiClient.request(
            endpoint: .getDeparturePlaces,
            responseType: [DeparturePlaceResponseDTO].self
        )
    }

    func createDeparturePlace(label: String, address: String, latitude: Double, longitude: Double) async throws -> DeparturePlaceResponseDTO {
        let body = CreateDeparturePlaceRequest(
            label: label,
            address: address,
            latitude: latitude,
            longitude: longitude
        )
        return try await apiClient.request(
            endpoint: .createDeparturePlace,
            body: body,
            responseType: DeparturePlaceResponseDTO.self
        )
    }

    func updateDeparturePlace(id: Int64, label: String?, address: String?, latitude: Double?, longitude: Double?) async throws -> DeparturePlaceResponseDTO {
        let body = UpdateDeparturePlaceRequest(
            label: label,
            address: address,
            latitude: latitude,
            longitude: longitude
        )
        return try await apiClient.request(
            endpoint: .updateDeparturePlace(id: id),
            body: body,
            responseType: DeparturePlaceResponseDTO.self
        )
    }

    func deleteDeparturePlace(id: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .deleteDeparturePlace(id: id))
    }

    // MARK: - UserSettings

    func getUserSettings() async throws -> UserSettingsResponseDTO {
        return try await apiClient.request(
            endpoint: .getUserSettings,
            responseType: UserSettingsResponseDTO.self
        )
    }

    func updateUserSettings(defaultTransportType: TransportType?, parkingBufferMinutes: Int?, extraMinutes: Int?) async throws -> UserSettingsResponseDTO {
        let body = UpdateUserSettingsRequest(
            defaultTransportType: defaultTransportType,
            parkingBufferMinutes: parkingBufferMinutes,
            extraMinutes: extraMinutes
        )
        return try await apiClient.request(
            endpoint: .updateUserSettings,
            body: body,
            responseType: UserSettingsResponseDTO.self
        )
    }

    // MARK: - Tag

    func getTags() async throws -> [TagResponseDTO] {
        return try await apiClient.request(
            endpoint: .getTags,
            responseType: [TagResponseDTO].self
        )
    }

    func createTag(name: String, color: String) async throws -> TagResponseDTO {
        let body = CreateTagRequest(name: name, color: color)
        return try await apiClient.request(
            endpoint: .createTag,
            body: body,
            responseType: TagResponseDTO.self
        )
    }

    func updateTag(id: Int64, name: String?, color: String?) async throws -> TagResponseDTO {
        let body = UpdateTagRequest(name: name, color: color)
        return try await apiClient.request(
            endpoint: .updateTag(id: id),
            body: body,
            responseType: TagResponseDTO.self
        )
    }

    func deleteTag(id: Int64) async throws {
        try await apiClient.requestVoid(endpoint: .deleteTag(id: id))
    }
}
