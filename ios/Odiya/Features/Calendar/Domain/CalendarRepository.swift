import Foundation

protocol CalendarRepository {
    func getCalendarData(year: Int, month: Int) async throws -> [CalendarDayResponseDTO]
}
