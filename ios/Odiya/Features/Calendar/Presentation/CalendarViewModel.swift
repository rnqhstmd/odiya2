import Foundation
import SwiftUI

@MainActor
final class CalendarViewModel: ObservableObject {

    // MARK: - Published State

    @Published var selectedDate: Date = Date()
    @Published var currentMonth: Date = Date() {
        didSet {
            let components = koreanCalendar.dateComponents([.year, .month], from: currentMonth)
            if let year = components.year, let month = components.month {
                Task { await loadMonth(year: year, month: month) }
            }
        }
    }
    @Published var appointments: [Appointment] = []
    @Published var isLoading: Bool = false
    @Published var errorMessage: String? = nil

    // MARK: - Private

    private let repository: CalendarRepository

    private let koreanCalendar: Calendar = {
        var cal = Calendar(identifier: .gregorian)
        cal.locale = Locale(identifier: "ko_KR")
        cal.firstWeekday = 2 // 월요일 시작
        return cal
    }()

    private static let isoDateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        f.locale = Locale(identifier: "en_US_POSIX")
        f.timeZone = TimeZone(identifier: "Asia/Seoul")
        return f
    }()

    private static let isoDateTimeFormatter: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return f
    }()

    private static let isoDateTimeFormatterNoFraction: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime]
        return f
    }()

    // MARK: - Init

    init(repository: CalendarRepository = CalendarRepositoryImpl()) {
        self.repository = repository
        let components = Calendar.current.dateComponents([.year, .month], from: Date())
        if let year = components.year, let month = components.month {
            Task { await loadMonth(year: year, month: month) }
        }
    }

    // MARK: - Load

    func loadMonth(year: Int, month: Int) async {
        isLoading = true
        errorMessage = nil
        defer { isLoading = false }
        do {
            let days = try await repository.getCalendarData(year: year, month: month)
            appointments = days.flatMap { day in
                day.appointments.map { dto in
                    appointmentFromDTO(dto)
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    // MARK: - DTO → Domain Mapping

    private func appointmentFromDTO(_ dto: CalendarAppointmentDTO) -> Appointment {
        let dateTime = Self.isoDateTimeFormatter.date(from: dto.dateTime)
            ?? Self.isoDateTimeFormatterNoFraction.date(from: dto.dateTime)
            ?? Date()

        // tagColor가 있으면 임시 Tag를 생성해 participants에 부여 (dot/color 표시용)
        let tag: Tag? = dto.tagColor.map { hex in
            Tag(id: hex, name: "", colorHex: hex, isDefault: false)
        }
        let participant = Participant(
            id: dto.id,
            nickname: "",
            profileImageUrl: nil,
            status: .accepted,
            isHost: false,
            tag: tag
        )

        return Appointment(
            id: dto.id,
            name: dto.name,
            placeName: dto.placeName,
            placeAddress: "",
            latitude: 0,
            longitude: 0,
            dateTime: dateTime,
            status: .confirmed,
            participants: [participant],
            hostId: 0,
            transportType: .transit,
            durationMinutes: nil,
            departurePlaceLabel: nil,
            departureAlertAt: nil
        )
    }

    // MARK: - Query

    func appointments(for date: Date) -> [Appointment] {
        appointments.filter { koreanCalendar.isDate($0.dateTime, inSameDayAs: date) }
    }

    func tags(for date: Date) -> [Tag] {
        let dayAppointments = appointments(for: date)
        var seen = Set<String>()
        var result: [Tag] = []
        for appointment in dayAppointments {
            for participant in appointment.participants {
                guard let tag = participant.tag else { continue }
                if seen.insert(tag.id).inserted {
                    result.append(tag)
                }
            }
        }
        return result
    }

    func hasAppointments(on date: Date) -> Bool {
        appointments(for: date).isEmpty == false
    }

    // MARK: - Navigation

    func goToToday() {
        let today = Date()
        selectedDate = today
        currentMonth = today
    }

    func moveMonth(by offset: Int) {
        guard let newMonth = koreanCalendar.date(byAdding: .month, value: offset, to: currentMonth) else { return }
        currentMonth = newMonth
    }

    func moveWeek(by offset: Int) {
        guard let newDate = koreanCalendar.date(byAdding: .weekOfYear, value: offset, to: selectedDate) else { return }
        selectedDate = newDate
        currentMonth = newDate
    }

    // MARK: - Calendar Helpers

    /// 해당 월의 첫 번째 날
    func startOfMonth(for date: Date) -> Date {
        let components = koreanCalendar.dateComponents([.year, .month], from: date)
        return koreanCalendar.date(from: components) ?? date
    }

    /// 해당 월의 날 수
    func daysInMonth(for date: Date) -> Int {
        koreanCalendar.range(of: .day, in: .month, for: date)?.count ?? 30
    }

    /// 월 시작 전 빈 셀 수 (월요일 기준)
    func leadingEmptyDays(for date: Date) -> Int {
        let start = startOfMonth(for: date)
        // weekday: 1=일, 2=월, ..., 7=토 → 월요일 시작이므로 (weekday + 5) % 7
        let weekday = koreanCalendar.component(.weekday, from: start)
        return (weekday + 5) % 7
    }

    /// 해당 월의 모든 날짜 배열 (빈 셀 포함)
    func calendarDays(for month: Date) -> [Date?] {
        let leading = leadingEmptyDays(for: month)
        let days = daysInMonth(for: month)
        let start = startOfMonth(for: month)

        var result: [Date?] = Array(repeating: nil, count: leading)
        for i in 0..<days {
            if let date = koreanCalendar.date(byAdding: .day, value: i, to: start) {
                result.append(date)
            }
        }
        // 6주 그리드 채우기
        let total = 42
        while result.count < total {
            result.append(nil)
        }
        return result
    }

    /// 해당 주의 날짜 배열 (월~일 7일)
    func weekDays(for date: Date) -> [Date] {
        // 해당 날짜가 속한 주의 월요일 찾기
        var components = koreanCalendar.dateComponents([.yearForWeekOfYear, .weekOfYear], from: date)
        components.weekday = 2 // 월요일
        let monday = koreanCalendar.date(from: components) ?? date
        return (0..<7).compactMap { koreanCalendar.date(byAdding: .day, value: $0, to: monday) }
    }

    // MARK: - Display Helpers

    func monthTitle(for date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy년 M월"
        return formatter.string(from: date)
    }

    var korCalendar: Calendar { koreanCalendar }
}
