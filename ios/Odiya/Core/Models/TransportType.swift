import Foundation

enum TransportType: String, CaseIterable, Identifiable, Codable {
    case carParking = "CAR_PARKING"
    case carPickup  = "CAR_PICKUP"
    case transit    = "TRANSIT"
    case walking    = "WALKING"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .carParking: return "자동차 (주차)"
        case .carPickup:  return "자동차 (픽업)"
        case .transit:    return "대중교통"
        case .walking:    return "도보"
        }
    }

    var shortName: String {
        switch self {
        case .carParking: return "주차"
        case .carPickup:  return "픽업"
        case .transit:    return "대중교통"
        case .walking:    return "도보"
        }
    }

    var iconName: String {
        switch self {
        case .carParking: return "car.fill"
        case .carPickup:  return "car.side"
        case .transit:    return "bus.fill"
        case .walking:    return "figure.walk"
        }
    }
}
