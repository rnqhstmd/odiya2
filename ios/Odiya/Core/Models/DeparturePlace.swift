import Foundation

struct DeparturePlace: Identifiable, Equatable, Hashable {
    let id: Int64
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

    init(id: Int64, label: String, address: String, latitude: Double, longitude: Double) {
        self.id = id
        self.label = label
        self.address = address
        self.latitude = latitude
        self.longitude = longitude
    }

    init(dto: DeparturePlaceResponseDTO) {
        self.id = dto.id
        self.label = dto.label
        self.address = dto.address
        self.latitude = dto.latitude
        self.longitude = dto.longitude
    }
}
