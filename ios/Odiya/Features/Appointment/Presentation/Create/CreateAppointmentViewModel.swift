import Foundation
import Combine

@MainActor
final class CreateAppointmentViewModel: ObservableObject {

    // MARK: - Step 1
    @Published var name: String = ""
    @Published var date: Date = Date()
    @Published var time: Date = Date()

    // MARK: - Step 2
    @Published var selectedPlace: PlaceResponseDTO? = nil
    @Published var placeSearchText: String = ""
    @Published var searchedPlaces: [PlaceResponseDTO] = []
    @Published var isSearchingPlaces: Bool = false
    private var placeSearchPage: Int = 1
    @Published var hasNextPlaces: Bool = false

    // MARK: - Step 3
    @Published var selectedFriends: Set<Friend> = []
    @Published var friendSearchText: String = ""
    @Published var allFriends: [Friend] = []
    @Published var isLoadingFriends: Bool = false

    // MARK: - Step 4
    @Published var transportType: TransportType = .transit
    @Published var departurePlace: DeparturePlace? = nil
    @Published var showDeparturePlaceSearch: Bool = false
    @Published var departurePlaces: [DeparturePlace] = []

    // MARK: - Alert
    @Published var showAlert: Bool = false
    @Published var alertTitle: String = ""
    @Published var alertMessage: String = ""
    @Published var isCreated: Bool = false
    @Published var isCreating: Bool = false

    // MARK: - Dependencies

    private let repository: AppointmentRepository
    private let friendRepository: FriendRepository
    private let settingsRepository: SettingsRepository

    // MARK: - Init

    init(
        repository: AppointmentRepository = AppointmentRepositoryImpl(),
        friendRepository: FriendRepository = FriendRepositoryImpl(),
        settingsRepository: SettingsRepository = SettingsRepositoryImpl()
    ) {
        self.repository = repository
        self.friendRepository = friendRepository
        self.settingsRepository = settingsRepository
        Task {
            await loadFriends()
            await loadDeparturePlaces()
        }
    }

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

    var filteredPlaces: [PlaceResponseDTO] {
        searchedPlaces
    }

    var availableFriends: [Friend] {
        let accepted = allFriends.filter { $0.status == .accepted }
        if friendSearchText.isEmpty { return accepted }
        return accepted.filter {
            $0.nickname.localizedCaseInsensitiveContains(friendSearchText)
        }
    }

    // MARK: - Place Search

    func searchPlaces() async {
        let keyword = placeSearchText.trimmingCharacters(in: .whitespaces)
        guard !keyword.isEmpty else {
            searchedPlaces = []
            return
        }
        isSearchingPlaces = true
        placeSearchPage = 1
        defer { isSearchingPlaces = false }
        do {
            let result = try await repository.searchPlaces(keyword: keyword, page: 1)
            searchedPlaces = result.places
            hasNextPlaces = result.hasNext
        } catch {
            searchedPlaces = []
        }
    }

    func loadMorePlaces() async {
        guard hasNextPlaces, !isSearchingPlaces else { return }
        let keyword = placeSearchText.trimmingCharacters(in: .whitespaces)
        guard !keyword.isEmpty else { return }
        isSearchingPlaces = true
        defer { isSearchingPlaces = false }
        do {
            placeSearchPage += 1
            let result = try await repository.searchPlaces(keyword: keyword, page: placeSearchPage)
            searchedPlaces += result.places
            hasNextPlaces = result.hasNext
        } catch {
            placeSearchPage -= 1
        }
    }

    // MARK: - Friend Loading

    func loadFriends() async {
        isLoadingFriends = true
        defer { isLoadingFriends = false }
        do {
            let dtos = try await friendRepository.getFriends(tagId: nil)
            allFriends = dtos.map { Friend(from: $0) }
        } catch {
            allFriends = []
        }
    }

    // MARK: - Departure Place Loading

    func loadDeparturePlaces() async {
        do {
            let dtos = try await settingsRepository.getDeparturePlaces()
            departurePlaces = dtos.map { DeparturePlace(dto: $0) }
        } catch {
            departurePlaces = []
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

    func createAppointment() async {
        guard let place = selectedPlace else { return }
        isCreating = true
        defer { isCreating = false }

        let participantIds = selectedFriends.map { $0.id }

        // DeparturePlace.id is a String label (e.g. "home"), not a backend Int64 id.
        // departurePlaceId requires a backend-registered place id; pass nil unless user
        // has selected a place via the backend place search (future integration).
        let request = CreateAppointmentRequest(
            name: name,
            placeName: place.name,
            placeAddress: place.roadAddress ?? place.address,
            latitude: place.latitude,
            longitude: place.longitude,
            dateTime: combinedDateTime,
            participantIds: participantIds,
            transportType: transportType.rawValue,
            departurePlaceId: departurePlace?.id
        )

        do {
            _ = try await repository.createAppointment(request: request)
            alertTitle = "약속 생성 완료"
            alertMessage = "약속이 생성되었습니다!"
            isCreated = true
            showAlert = true
        } catch {
            alertTitle = "오류"
            alertMessage = error.localizedDescription
            isCreated = false
            showAlert = true
        }
    }
}
