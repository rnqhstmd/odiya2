import SwiftUI

enum OdiyaColors {
    // MARK: - 오디 팔레트
    static let odiya900 = Color(hex: 0x2D1B4E)
    static let odiya700 = Color(hex: 0x5B3A8C) // Primary
    static let odiya500 = Color(hex: 0x8B5FBF) // Secondary
    static let odiya300 = Color(hex: 0xC9A8E8) // Tertiary
    static let odiya100 = Color(hex: 0xF0E6F7) // Surface
    static let odiya50  = Color(hex: 0xF8F3FC) // Background Tint

    // MARK: - 기능별
    static let primary = odiya700
    static let secondary = odiya500
    static let nudge = Color(hex: 0xD94F8A)
    static let kakaoYellow = Color(hex: 0xFEE500)

    // MARK: - iOS 시맨틱
    static let danger = Color(hex: 0xFF3B30)
    static let success = Color(hex: 0x34C759)
    static let warning = Color(hex: 0xFF9500)

    // MARK: - 태그 프리셋 12색
    static let tagPresets: [Color] = [
        Color(hex: 0xFF3B30), // 빨강
        Color(hex: 0xFF9500), // 주황
        Color(hex: 0xFFCC00), // 노랑
        Color(hex: 0x34C759), // 초록
        Color(hex: 0x00C7BE), // 민트
        Color(hex: 0x5AC8FA), // 하늘
        Color(hex: 0x007AFF), // 파랑
        Color(hex: 0xAF52DE), // 보라
        Color(hex: 0xC9A8E8), // 라벤더
        Color(hex: 0xFF2D55), // 핑크
        Color(hex: 0xA2845E), // 갈색
        Color(hex: 0x8E8E93), // 회색
    ]

    // MARK: - 태그 기본 색상
    static let tagFriend = Color(hex: 0x5AC8FA)   // 친구 - 하늘
    static let tagLover  = Color(hex: 0xFF2D55)    // 연인 - 핑크
    static let tagFamily = Color(hex: 0x34C759)    // 가족 - 초록
}

// MARK: - Color Extension

extension Color {
    init(hex: UInt, opacity: Double = 1.0) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: opacity
        )
    }

    init(hexString: String) {
        let hex = hexString.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        self.init(hex: UInt(int))
    }
}
