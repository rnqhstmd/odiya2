import Foundation

protocol SettingsRepository {

    // MARK: - DeparturePlace
    func getDeparturePlaces() async throws -> [DeparturePlaceResponseDTO]
    func createDeparturePlace(label: String, address: String, latitude: Double, longitude: Double) async throws -> DeparturePlaceResponseDTO
    func updateDeparturePlace(id: Int64, label: String?, address: String?, latitude: Double?, longitude: Double?) async throws -> DeparturePlaceResponseDTO
    func deleteDeparturePlace(id: Int64) async throws

    // MARK: - UserSettings
    func getUserSettings() async throws -> UserSettingsResponseDTO
    func updateUserSettings(defaultTransportType: TransportType?, parkingBufferMinutes: Int?, extraMinutes: Int?) async throws -> UserSettingsResponseDTO

    // MARK: - Tag
    func getTags() async throws -> [TagResponseDTO]
    func createTag(name: String, color: String) async throws -> TagResponseDTO
    func updateTag(id: Int64, name: String?, color: String?) async throws -> TagResponseDTO
    func deleteTag(id: Int64) async throws
}
