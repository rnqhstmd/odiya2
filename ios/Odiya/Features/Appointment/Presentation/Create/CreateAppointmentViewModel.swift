import Foundation
import Combine

@MainActor
final class CreateAppointmentViewModel: ObservableObject {

    // MARK: - Step 1
    @Published var name: String = ""
    @Published var date: Date = Date()
    @Published var time: Date = Date()

    // MARK: - Step 2
    @Published var selectedPlace: MockPlace? = nil
    @Published var placeSearchText: String = ""

    // MARK: - Step 3
    @Published var selectedFriends: Set<Friend> = []
    @Published var friendSearchText: String = ""

    // MARK: - Step 4
    @Published var transportType: TransportType = .transit
    @Published var departurePlace: DeparturePlace? = nil
    @Published var showDeparturePlaceSearch: Bool = false

    // MARK: - Alert
    @Published var showAlert: Bool = false
    @Published var alertTitle: String = ""
    @Published var alertMessage: String = ""
    @Published var isCreated: Bool = false

    // MARK: - Validation

    var canProceedToStep2: Bool {
        !name.trimmingCharacters(in: .whitespaces).isEmpty
    }

    var canProceedToStep3: Bool {
        selectedPlace != nil
    }

    var canProceedToStep4: Bool {
        !selectedFriends.isEmpty
    }

    // MARK: - Computed

    var combinedDateTime: Date {
        let calendar = Calendar.current
        let dateComponents = calendar.dateComponents([.year, .month, .day], from: date)
        let timeComponents = calendar.dateComponents([.hour, .minute], from: time)
        var combined = DateComponents()
        combined.year   = dateComponents.year
        combined.month  = dateComponents.month
        combined.day    = dateComponents.day
        combined.hour   = timeComponents.hour
        combined.minute = timeComponents.minute
        return calendar.date(from: combined) ?? date
    }

    var filteredPlaces: [MockPlace] {
        if placeSearchText.isEmpty { return MockPlace.samples }
        return MockPlace.samples.filter {
            $0.name.localizedCaseInsensitiveContains(placeSearchText)
        }
    }

    var availableFriends: [Friend] {
        let accepted = MockData.friends.filter { $0.status == .accepted }
        if friendSearchText.isEmpty { return accepted }
        return accepted.filter {
            $0.nickname.localizedCaseInsensitiveContains(friendSearchText)
        }
    }

    // MARK: - Actions

    func toggleFriend(_ friend: Friend) {
        if selectedFriends.contains(friend) {
            selectedFriends.remove(friend)
        } else {
            selectedFriends.insert(friend)
        }
    }

    func selectDeparturePlace(_ place: DeparturePlace) {
        departurePlace = place
    }

    func clearDeparturePlace() {
        departurePlace = nil
    }

    func createAppointment() {
        alertTitle = "약속 생성 완료"
        alertMessage = "약속이 생성되었습니다!"
        isCreated = true
        showAlert = true
    }
}

// MARK: - MockPlace (local model for Step 2)

struct MockPlace: Identifiable, Equatable {
    let id: String
    let name: String
    let address: String

    static let samples: [MockPlace] = [
        MockPlace(id: "1", name: "강남역",     address: "서울 강남구 강남대로 396"),
        MockPlace(id: "2", name: "홍대입구역", address: "서울 마포구 양화로 160"),
        MockPlace(id: "3", name: "이태원",     address: "서울 용산구 이태원로 177"),
        MockPlace(id: "4", name: "건대입구역", address: "서울 광진구 아차산로 272"),
        MockPlace(id: "5", name: "합정역",     address: "서울 마포구 양화로 45"),
    ]
}
