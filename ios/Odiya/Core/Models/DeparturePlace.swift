import Foundation

struct DeparturePlace: Identifiable, Equatable, Hashable {
    let id: String
    var label: String
    var address: String
    var latitude: Double
    var longitude: Double

    var iconName: String {
        switch label {
        case "집":   return "house.fill"
        case "회사": return "building.2.fill"
        default:     return "mappin.circle.fill"
        }
    }
}
