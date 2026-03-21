import Foundation

final class CalendarRepositoryImpl: CalendarRepository {

    private let apiClient: APIClient

    init(apiClient: APIClient = .shared) {
        self.apiClient = apiClient
    }

    func getCalendarData(year: Int, month: Int) async throws -> [CalendarDayResponseDTO] {
        try await apiClient.request(
            endpoint: .getCalendarData(year: year, month: month),
            responseType: [CalendarDayResponseDTO].self
        )
    }
}
