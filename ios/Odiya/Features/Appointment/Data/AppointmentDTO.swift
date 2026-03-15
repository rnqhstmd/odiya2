import Foundation

// MARK: - Request DTOs

private let sharedISOEncoder: ISO8601DateFormatter = {
    let f = ISO8601DateFormatter()
    f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
    return f
}()

struct CreateAppointmentRequest: Encodable {
    let name: String
    let placeName: String
    let placeAddress: String
    let latitude: Double
    let longitude: Double
    let dateTime: Date
    let participantIds: [Int64]
    let transportType: String
    let departurePlaceId: Int64?

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(name, forKey: .name)
        try container.encode(placeName, forKey: .placeName)
        try container.encode(placeAddress, forKey: .placeAddress)
        try container.encode(latitude, forKey: .latitude)
        try container.encode(longitude, forKey: .longitude)
        try container.encode(sharedISOEncoder.string(from: dateTime), forKey: .dateTime)
        try container.encode(participantIds, forKey: .participantIds)
        try container.encode(transportType, forKey: .transportType)
        try container.encodeIfPresent(departurePlaceId, forKey: .departurePlaceId)
    }

    enum CodingKeys: String, CodingKey {
        case name, placeName, placeAddress, latitude, longitude
        case dateTime, participantIds, transportType, departurePlaceId
    }
}

struct UpdateAppointmentRequest: Encodable {
    let name: String?
    let placeName: String?
    let placeAddress: String?
    let latitude: Double?
    let longitude: Double?
    let dateTime: Date?

    func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encodeIfPresent(name, forKey: .name)
        try container.encodeIfPresent(placeName, forKey: .placeName)
        try container.encodeIfPresent(placeAddress, forKey: .placeAddress)
        try container.encodeIfPresent(latitude, forKey: .latitude)
        try container.encodeIfPresent(longitude, forKey: .longitude)
        if let dateTime = dateTime {
            try container.encode(sharedISOEncoder.string(from: dateTime), forKey: .dateTime)
        }
    }

    enum CodingKeys: String, CodingKey {
        case name, placeName, placeAddress, latitude, longitude, dateTime
    }
}

struct InviteRequest: Encodable {
    let userIds: [Int64]
}

struct UpdateDepartureRequest: Encodable {
    let departurePlaceId: Int64?
    let transportType: String?
}

struct NudgeRequest: Encodable {
    let targetUserIds: [Int64]
}

// MARK: - Response DTOs

struct ParticipantResponseDTO: Decodable {
    let userId: Int64
    let nickname: String
    let profileImageUrl: String?
    let status: String
    let isHost: Bool
}

struct AppointmentResponseDTO: Decodable {
    let id: Int64
    let name: String
    let placeName: String
    let placeAddress: String
    let latitude: Double
    let longitude: Double
    let dateTime: String
    let status: String
    let hostId: Int64
    let transportType: String?
    let durationMinutes: Int?
    let departureAlertAt: String?
    let departurePlaceLabel: String?
    let participants: [ParticipantResponseDTO]
}

struct AppointmentDetailResponseDTO: Decodable {
    let id: Int64
    let name: String
    let placeName: String
    let placeAddress: String
    let latitude: Double
    let longitude: Double
    let dateTime: String
    let status: String
    let hostId: Int64
    let transportType: String?
    let durationMinutes: Int?
    let departureAlertAt: String?
    let departurePlaceLabel: String?
    let participants: [ParticipantResponseDTO]
    let canNudge: Bool
    let nudgeCooldownSeconds: Int?
    let isHost: Bool
    let myStatus: String
}

struct AppointmentListResponseDTO: Decodable {
    let appointments: [AppointmentResponseDTO]
    let hasNext: Bool
}

struct DepartureUpdateResponseDTO: Decodable {
    let durationMinutes: Int?
    let departureAlertAt: String?
    let transportType: String?
    let departurePlaceLabel: String?
}

// MARK: - Place DTOs

struct PlaceResponseDTO: Decodable, Identifiable, Equatable {
    let kakaoPlaceId: String
    let name: String
    let address: String
    let roadAddress: String?
    let category: String?
    let latitude: Double
    let longitude: Double

    var id: String { kakaoPlaceId }
}

struct PlaceSearchResponseDTO: Decodable {
    let places: [PlaceResponseDTO]
    let hasNext: Bool
}

// MARK: - Domain Model Conversions

private let isoParser: ISO8601DateFormatter = {
    let f = ISO8601DateFormatter()
    f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
    return f
}()

private let isoParserNoFractional: ISO8601DateFormatter = {
    let f = ISO8601DateFormatter()
    f.formatOptions = [.withInternetDateTime]
    return f
}()

private func parseDate(_ string: String) -> Date? {
    isoParser.date(from: string)
        ?? isoParserNoFractional.date(from: string)
}

// MARK: - Shared Conversion Protocol

protocol AppointmentMappable {
    var id: Int64 { get }
    var name: String { get }
    var placeName: String { get }
    var placeAddress: String { get }
    var latitude: Double { get }
    var longitude: Double { get }
    var dateTime: String { get }
    var status: String { get }
    var hostId: Int64 { get }
    var transportType: String? { get }
    var durationMinutes: Int? { get }
    var departureAlertAt: String? { get }
    var departurePlaceLabel: String? { get }
    var participants: [ParticipantResponseDTO] { get }
}

extension AppointmentMappable {
    func toDomain() -> Appointment {
        let parsedParticipants = participants.map { p -> Participant in
            Participant(
                id: p.userId,
                nickname: p.nickname,
                profileImageUrl: p.profileImageUrl,
                status: ParticipantStatus(rawValue: p.status) ?? .pending,
                isHost: p.isHost,
                tag: nil
            )
        }
        return Appointment(
            id: id,
            name: name,
            placeName: placeName,
            placeAddress: placeAddress,
            latitude: latitude,
            longitude: longitude,
            dateTime: parseDate(dateTime) ?? Date(),
            status: AppointmentStatus(rawValue: status) ?? .pending,
            participants: parsedParticipants,
            hostId: hostId,
            transportType: TransportType(rawValue: transportType ?? "") ?? .transit,
            durationMinutes: durationMinutes,
            departurePlaceLabel: departurePlaceLabel,
            departureAlertAt: departureAlertAt.flatMap { parseDate($0) }
        )
    }
}

extension AppointmentResponseDTO: AppointmentMappable {}
extension AppointmentDetailResponseDTO: AppointmentMappable {}
