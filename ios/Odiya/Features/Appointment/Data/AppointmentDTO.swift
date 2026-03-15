import Foundation

// MARK: - Request DTOs

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
        let iso = ISO8601DateFormatter()
        iso.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        try container.encode(iso.string(from: dateTime), forKey: .dateTime)
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
            let iso = ISO8601DateFormatter()
            iso.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
            try container.encode(iso.string(from: dateTime), forKey: .dateTime)
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

private func parseDate(_ string: String) -> Date {
    isoParser.date(from: string)
        ?? isoParserNoFractional.date(from: string)
        ?? Date()
}

extension AppointmentResponseDTO {
    func toDomain() -> Appointment {
        let parsedParticipants = participants.map { p -> Participant in
            let pStatus = ParticipantStatus(rawValue: p.status) ?? .pending
            return Participant(
                id: p.userId,
                nickname: p.nickname,
                profileImageUrl: p.profileImageUrl,
                status: pStatus,
                isHost: p.isHost,
                tag: nil
            )
        }
        let appStatus = AppointmentStatus(rawValue: status) ?? .pending
        let transport = TransportType(rawValue: transportType ?? "") ?? .transit
        return Appointment(
            id: id,
            name: name,
            placeName: placeName,
            placeAddress: placeAddress,
            latitude: latitude,
            longitude: longitude,
            dateTime: parseDate(dateTime),
            status: appStatus,
            participants: parsedParticipants,
            hostId: hostId,
            transportType: transport,
            durationMinutes: durationMinutes,
            departurePlaceLabel: departurePlaceLabel,
            departureAlertAt: departureAlertAt.map { parseDate($0) }
        )
    }
}

extension AppointmentDetailResponseDTO {
    func toDomain() -> Appointment {
        let parsedParticipants = participants.map { p -> Participant in
            let pStatus = ParticipantStatus(rawValue: p.status) ?? .pending
            return Participant(
                id: p.userId,
                nickname: p.nickname,
                profileImageUrl: p.profileImageUrl,
                status: pStatus,
                isHost: p.isHost,
                tag: nil
            )
        }
        let appStatus = AppointmentStatus(rawValue: status) ?? .pending
        let transport = TransportType(rawValue: transportType ?? "") ?? .transit
        return Appointment(
            id: id,
            name: name,
            placeName: placeName,
            placeAddress: placeAddress,
            latitude: latitude,
            longitude: longitude,
            dateTime: parseDate(dateTime),
            status: appStatus,
            participants: parsedParticipants,
            hostId: hostId,
            transportType: transport,
            durationMinutes: durationMinutes,
            departurePlaceLabel: departurePlaceLabel,
            departureAlertAt: departureAlertAt.map { parseDate($0) }
        )
    }
}
