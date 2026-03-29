import Foundation

// MARK: - Notification

enum NotificationType: String, CaseIterable {
    case friendRequest      = "FRIEND_REQUEST"
    case appointmentInvite  = "APPOINTMENT_INVITE"
    case departureAlert     = "DEPARTURE_ALERT"
    case nudge              = "NUDGE"
    case appointmentConfirmed = "APPOINTMENT_CONFIRMED"
    case appointmentCancelled = "APPOINTMENT_CANCELLED"
    case friendAccepted     = "FRIEND_ACCEPTED"
    case unknown            = "UNKNOWN"

    var iconName: String {
        switch self {
        case .friendRequest:        return "person.badge.plus"
        case .friendAccepted:       return "person.badge.checkmark"
        case .appointmentInvite:    return "calendar.badge.plus"
        case .departureAlert:       return "bell.badge.fill"
        case .nudge:                return "megaphone.fill"
        case .appointmentConfirmed: return "checkmark.circle.fill"
        case .appointmentCancelled: return "xmark.circle.fill"
        case .unknown:              return "bell"
        }
    }
}

struct AppNotification: Identifiable {
    let id: String
    let type: NotificationType
    let title: String
    let body: String
    let referenceId: Int64?
    let createdAt: Date
    var isRead: Bool

    var timeAgoText: String {
        let interval = Date().timeIntervalSince(createdAt)
        let minutes = Int(interval / 60)
        if minutes < 1 { return "방금 전" }
        if minutes < 60 { return "\(minutes)분 전" }
        let hours = minutes / 60
        if hours < 24 { return "\(hours)시간 전" }
        let days = hours / 24
        return "\(days)일 전"
    }

    init(id: String, type: NotificationType, title: String, body: String, referenceId: Int64? = nil, createdAt: Date, isRead: Bool) {
        self.id = id
        self.type = type
        self.title = title
        self.body = body
        self.referenceId = referenceId
        self.createdAt = createdAt
        self.isRead = isRead
    }

    init(dto: NotificationResponseDTO) {
        self.id = String(dto.id)
        self.type = NotificationType(rawValue: dto.type) ?? .unknown
        self.title = dto.title
        self.body = dto.body
        self.referenceId = dto.referenceId
        self.isRead = dto.isRead

        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = formatter.date(from: dto.createdAt) {
            self.createdAt = date
        } else {
            formatter.formatOptions = [.withInternetDateTime]
            self.createdAt = formatter.date(from: dto.createdAt) ?? Date.distantPast
        }
    }
}

// MARK: - Appointment

enum AppointmentStatus: String, Codable, CaseIterable {
    case pending    = "PENDING"
    case confirmed  = "CONFIRMED"
    case completed  = "COMPLETED"
    case cancelled  = "CANCELLED"

    var displayName: String {
        switch self {
        case .pending:   return "대기중"
        case .confirmed: return "확정"
        case .completed: return "완료"
        case .cancelled: return "취소"
        }
    }
}

enum ParticipantStatus: String {
    case accepted = "ACCEPTED"
    case pending  = "PENDING"
    case rejected = "REJECTED"
}

struct Participant: Identifiable, Equatable, Hashable {
    let id: Int64
    var nickname: String
    var profileImageUrl: String?
    var status: ParticipantStatus
    var isHost: Bool
    var tag: Tag?
}

struct Appointment: Identifiable, Equatable {
    let id: Int64
    var name: String
    var placeName: String
    var placeAddress: String
    var latitude: Double
    var longitude: Double
    var dateTime: Date
    var status: AppointmentStatus
    var participants: [Participant]
    var hostId: Int64
    var transportType: TransportType
    var durationMinutes: Int?
    var departurePlaceLabel: String?
    var departureAlertAt: Date?
    var currentUserIsHost: Bool = false

    var isUpcoming: Bool {
        status == .confirmed && dateTime > Date()
    }

    var isImminent: Bool {
        guard isUpcoming else { return false }
        let minutesUntil = Calendar.current.dateComponents([.minute], from: Date(), to: dateTime).minute ?? 0
        return minutesUntil <= 120
    }

    var minutesUntilDeparture: Int? {
        guard let alertAt = departureAlertAt else { return nil }
        let minutes = Calendar.current.dateComponents([.minute], from: Date(), to: alertAt).minute ?? 0
        return max(0, minutes)
    }

    var canNudge: Bool {
        guard isUpcoming else { return false }
        let minutesUntil = Calendar.current.dateComponents([.minute], from: Date(), to: dateTime).minute ?? 0
        return minutesUntil <= 30 && minutesUntil > 0
    }
}
