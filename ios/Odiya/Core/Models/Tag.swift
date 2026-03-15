import SwiftUI

struct Tag: Identifiable, Equatable, Hashable {
    let id: String
    var name: String
    var colorHex: String
    var isDefault: Bool

    var color: Color {
        Color(hexString: colorHex)
    }

    // MARK: - 기본 이니셜라이저

    init(id: String, name: String, colorHex: String, isDefault: Bool) {
        self.id = id
        self.name = name
        self.colorHex = colorHex
        self.isDefault = isDefault
    }

    // MARK: - 기본 태그

    static let friend = Tag(id: "default-friend", name: "친구", colorHex: "#5AC8FA", isDefault: true)
    static let lover  = Tag(id: "default-lover", name: "연인", colorHex: "#FF2D55", isDefault: true)
    static let family = Tag(id: "default-family", name: "가족", colorHex: "#34C759", isDefault: true)

    static let defaults: [Tag] = [friend, lover, family]

    // MARK: - DTO 변환

    init(from dto: TagResponseDTO) {
        self.id = String(dto.id)
        self.name = dto.name
        self.colorHex = dto.color
        self.isDefault = dto.isDefault
    }
}
