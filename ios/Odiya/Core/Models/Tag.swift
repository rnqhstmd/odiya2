import SwiftUI

struct Tag: Identifiable, Equatable, Hashable {
    let id: Int64
    var name: String
    var colorHex: String
    var isDefault: Bool

    var color: Color {
        Color(hexString: colorHex)
    }

    init(id: Int64, name: String, colorHex: String, isDefault: Bool) {
        self.id = id
        self.name = name
        self.colorHex = colorHex
        self.isDefault = isDefault
    }

    init(dto: TagResponseDTO) {
        self.id = dto.id
        self.name = dto.name
        self.colorHex = dto.color
        self.isDefault = dto.isDefault
    }

    // MARK: - 기본 태그 (로컬 fallback용)

    static let friend = Tag(id: -1, name: "친구", colorHex: "#5AC8FA", isDefault: true)
    static let lover  = Tag(id: -2, name: "연인", colorHex: "#FF2D55", isDefault: true)
    static let family = Tag(id: -3, name: "가족", colorHex: "#34C759", isDefault: true)

    static let defaults: [Tag] = [friend, lover, family]
}
