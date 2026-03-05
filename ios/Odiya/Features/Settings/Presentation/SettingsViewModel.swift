import SwiftUI

@MainActor
final class SettingsViewModel: ObservableObject {

    @Published var user: User = MockData.currentUser
    @Published var tags: [Tag] = MockData.tags
    @Published var departurePlaces: [DeparturePlace] = MockData.departurePlaces
    @Published var showLogoutAlert: Bool = false
    @Published var showDeleteAlert: Bool = false

    func logout() {
        // Mock: 실제 로그아웃 로직은 나중에 연동
        showLogoutAlert = false
    }

    func deleteAccount() {
        // Mock: 실제 회원 탈퇴 로직은 나중에 연동
        showDeleteAlert = false
    }

    func addTag(_ tag: Tag) {
        tags.append(tag)
    }

    func updateTag(_ tag: Tag) {
        if let index = tags.firstIndex(where: { $0.id == tag.id }) {
            tags[index] = tag
        }
    }

    func deleteTag(_ tag: Tag) {
        tags.removeAll { $0.id == tag.id }
    }

    func addDeparturePlace(_ place: DeparturePlace) {
        departurePlaces.append(place)
    }

    func deleteDeparturePlace(_ place: DeparturePlace) {
        departurePlaces.removeAll { $0.id == place.id }
    }
}
