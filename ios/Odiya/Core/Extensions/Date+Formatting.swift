import Foundation

extension Date {

    // MARK: - Cached Formatters

    private static let koreanFullFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "M월 d일 (E) a h:mm"
        return formatter
    }()

    private static let koreanDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "yyyy년 M월 d일 (E)"
        return formatter
    }()

    private static let koreanTimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ko_KR")
        formatter.dateFormat = "a h:mm"
        return formatter
    }()

    // MARK: - Formatted Strings

    /// "M월 d일 (E) a h:mm" — 약속 카드 등 한 줄 표시용
    var koreanFormatted: String {
        Self.koreanFullFormatter.string(from: self)
    }

    /// "yyyy년 M월 d일 (E)" — 약속 상세 날짜 표시용
    var koreanDateFormatted: String {
        Self.koreanDateFormatter.string(from: self)
    }

    /// "a h:mm" — 약속 상세 시간 표시용
    var koreanTimeFormatted: String {
        Self.koreanTimeFormatter.string(from: self)
    }
}
