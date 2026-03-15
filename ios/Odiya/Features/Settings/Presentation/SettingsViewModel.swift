import SwiftUI

@MainActor
final class SettingsViewModel: ObservableObject {

    @Published var tags: [Tag] = []
    @Published var departurePlaces: [DeparturePlace] = []
    @Published var userSettings: UserSettingsResponseDTO? = nil
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil
    @Published var showLogoutAlert: Bool = false
    @Published var showDeleteAlert: Bool = false

    private let repository: SettingsRepository

    init(repository: SettingsRepository = SettingsRepositoryImpl()) {
        self.repository = repository
    }

    // MARK: - Helpers

    private func fetch<T>(_ apiCall: () async throws -> T) async -> T? {
        do {
            return try await apiCall()
        } catch {
            errorMessage = error.localizedDescription
            return nil
        }
    }

    // MARK: - Load

    func loadData() async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }

        await withTaskGroup(of: Void.self) { group in
            group.addTask { await self.loadDeparturePlaces() }
            group.addTask { await self.loadTags() }
            group.addTask { await self.loadUserSettings() }
        }
    }

    private func loadDeparturePlaces() async {
        if let dtos = await fetch({ try await repository.getDeparturePlaces() }) {
            departurePlaces = dtos.map { DeparturePlace(dto: $0) }
        }
    }

    private func loadTags() async {
        if let dtos = await fetch({ try await repository.getTags() }) {
            tags = dtos.map { Tag(dto: $0) }
        }
    }

    private func loadUserSettings() async {
        userSettings = await fetch({ try await repository.getUserSettings() })
    }

    // MARK: - DeparturePlace CRUD

    func addDeparturePlace(label: String, address: String, latitude: Double, longitude: Double) async {
        isLoading = true
        defer { isLoading = false }
        if let dto = await fetch({
            try await repository.createDeparturePlace(
                label: label, address: address,
                latitude: latitude, longitude: longitude
            )
        }) {
            departurePlaces.append(DeparturePlace(dto: dto))
        }
    }

    func updateDeparturePlace(id: Int64, label: String, address: String, latitude: Double, longitude: Double) async {
        isLoading = true
        defer { isLoading = false }
        if let dto = await fetch({
            try await repository.updateDeparturePlace(
                id: id, label: label, address: address,
                latitude: latitude, longitude: longitude
            )
        }) {
            if let index = departurePlaces.firstIndex(where: { $0.id == id }) {
                departurePlaces[index] = DeparturePlace(dto: dto)
            }
        }
    }

    func deleteDeparturePlace(_ place: DeparturePlace) async {
        isLoading = true
        defer { isLoading = false }
        if let _ = await fetch({ try await repository.deleteDeparturePlace(id: place.id) }) {
            departurePlaces.removeAll { $0.id == place.id }
        }
    }

    // MARK: - Tag CRUD

    func addTag(name: String, color: String) async {
        isLoading = true
        defer { isLoading = false }
        if let dto = await fetch({ try await repository.createTag(name: name, color: color) }) {
            tags.append(Tag(dto: dto))
        }
    }

    func updateTag(id: Int64, name: String, color: String) async {
        isLoading = true
        defer { isLoading = false }
        if let dto = await fetch({ try await repository.updateTag(id: id, name: name, color: color) }) {
            if let index = tags.firstIndex(where: { $0.id == id }) {
                tags[index] = Tag(dto: dto)
            }
        }
    }

    func deleteTag(_ tag: Tag) async {
        isLoading = true
        defer { isLoading = false }
        if let _ = await fetch({ try await repository.deleteTag(id: tag.id) }) {
            tags.removeAll { $0.id == tag.id }
        }
    }

    // MARK: - Account

    func logout() {
        showLogoutAlert = false
        // TODO: TokenManager.shared.clear() + deactivateDevice 호출
    }

    func deleteAccount() {
        showDeleteAlert = false
        // TODO: 회원 탈퇴 API 호출
    }
}
