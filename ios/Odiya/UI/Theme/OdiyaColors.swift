import SwiftUI
import UIKit

/// 디자인 시스템 v2 컬러 토큰.
///
/// 두 계층으로 구성:
/// 1. **Raw palette** — `p50~p950` / `g50~g950` / `accent*` / `tag*` 등 불변 hex 값.
/// 2. **Semantic tokens** — `background` / `surface` / `text` 등 라이트·다크에 따라 자동 해석.
///
/// 다크모드는 Asset Catalog 없이 `Color(lightHex:darkHex:)` 동적 UIColor 헬퍼로 처리한다.
/// 기존 v1 토큰(`odiya700` / `nudge` / `tagFriend` 등)은 호환을 위해 유지한다 — 각 화면이
/// v2 토큰으로 마이그레이션될 때까지 제거하지 않는다.
enum OdiyaColors {

    // MARK: - v2 Primary palette (깊은 보라 11단)

    static let p50  = Color(hex: 0xF8F5FC)
    static let p100 = Color(hex: 0xF1EAF9)
    static let p200 = Color(hex: 0xE3D5F2)
    static let p300 = Color(hex: 0xC9B3E8)
    static let p400 = Color(hex: 0xA78BDA)
    static let p500 = Color(hex: 0x8B5FBF)
    static let p600 = Color(hex: 0x6B46C1)
    /// v2 Primary — 메인 CTA, 강조
    static let p700 = Color(hex: 0x4C2A8F)
    static let p800 = Color(hex: 0x3A1F6B)
    static let p900 = Color(hex: 0x261648)
    static let p950 = Color(hex: 0x1A0E33)

    // MARK: - v2 Neutrals (모던 그레이 13단)

    static let g50   = Color(hex: 0xFAFAFC)
    static let g100  = Color(hex: 0xF4F4F7)
    static let g150  = Color(hex: 0xEDEDF1)
    static let g200  = Color(hex: 0xE4E4EA)
    static let g300  = Color(hex: 0xD1D1D9)
    static let g400  = Color(hex: 0xB0B0BA)
    static let g500  = Color(hex: 0x8B8B96)
    static let g600  = Color(hex: 0x5C5C68)
    static let g700  = Color(hex: 0x3D3D47)
    static let g800  = Color(hex: 0x26262E)
    static let g900  = Color(hex: 0x17171C)
    static let g950  = Color(hex: 0x0B0B0F)

    // MARK: - v2 Accent (재촉 / 긴급 전용)

    /// 재촉 CTA, 긴급 강조
    static let accent     = Color(hex: 0xFF5E7A)
    static let accentDeep = Color(hex: 0xE23D5E)
    static let accentSoft = Color(hex: 0xFFE4E9)

    // MARK: - v2 Functional

    static let successV2 = Color(hex: 0x00C896)
    static let warningV2 = Color(hex: 0xFFA61E)
    static let dangerV2  = Color(hex: 0xFF4D4F)
    static let infoV2    = Color(hex: 0x2E7CF6)
    static let kakaoV2   = Color(hex: 0xFEE500)

    // MARK: - v2 Semantic tokens (Light / Dark 동적)

    /// 앱 최상위 배경
    static let background = Color(lightHex: 0xFAFAFC, darkHex: 0x0B0B11)
    /// 카드 · 시트 기본 표면
    static let surface    = Color(lightHex: 0xFFFFFF, darkHex: 0x17171E)
    /// 카드 위 카드(보조 표면)
    static let surface2   = Color(lightHex: 0xF4F4F7, darkHex: 0x1F1F28)
    /// 본문 텍스트
    static let textPrimary   = Color(lightHex: 0x0B0B11, darkHex: 0xF4F4F7)
    /// 보조 텍스트
    static let textSecondary = Color(
        lightUIColor: UIColor(red: 92/255, green: 92/255, blue: 104/255, alpha: 1.0),
        darkUIColor:  UIColor(white: 1.0, alpha: 0.65)
    )
    /// 힌트 · 비활성
    static let textTertiary  = Color(
        lightUIColor: UIColor(red: 144/255, green: 144/255, blue: 160/255, alpha: 1.0),
        darkUIColor:  UIColor(white: 1.0, alpha: 0.4)
    )
    /// 구분선 (알파 포함)
    static let separator       = Color(
        lightUIColor: UIColor(red: 17/255, green: 17/255, blue: 28/255, alpha: 0.07),
        darkUIColor:  UIColor(white: 1.0, alpha: 0.08)
    )
    static let separatorStrong = Color(
        lightUIColor: UIColor(red: 17/255, green: 17/255, blue: 28/255, alpha: 0.14),
        darkUIColor:  UIColor(white: 1.0, alpha: 0.14)
    )

    /// v2 Interactive — 링크·탭바 활성. 라이트=p600, 다크=p400
    static let interactive = Color(lightHex: 0x6B46C1, darkHex: 0xA78BDA)
    /// v2 Primary (semantic) — 메인 CTA. 라이트=p700, 다크=p500
    static let primaryV2 = Color(lightHex: 0x4C2A8F, darkHex: 0x8B5FBF)

    // MARK: - v2 태그 팔레트 (친구 태그 = 개인화 액센트, 8색)

    struct TagV2 {
        let key: String
        let name: String
        let solid: Color
        let soft: Color
    }

    static let tagLavender = TagV2(key: "lavender", name: "라벤더",   solid: Color(hex: 0xA78BDA), soft: Color(hex: 0xEDE4FA))
    static let tagRose     = TagV2(key: "rose",     name: "로즈",     solid: Color(hex: 0xFF8FA3), soft: Color(hex: 0xFFE0E7))
    static let tagSky      = TagV2(key: "sky",      name: "스카이",   solid: Color(hex: 0x66B2FF), soft: Color(hex: 0xDCEEFF))
    static let tagMint     = TagV2(key: "mint",     name: "민트",     solid: Color(hex: 0x3ECFAE), soft: Color(hex: 0xD4F5EC))
    static let tagAmber    = TagV2(key: "amber",    name: "앰버",     solid: Color(hex: 0xFFB547), soft: Color(hex: 0xFFEACC))
    static let tagCoral    = TagV2(key: "coral",    name: "코랄",     solid: Color(hex: 0xFF7A6B), soft: Color(hex: 0xFFE1DD))
    static let tagOlive    = TagV2(key: "olive",    name: "올리브",   solid: Color(hex: 0x8FAD62), soft: Color(hex: 0xE5EED6))
    static let tagSlate    = TagV2(key: "slate",    name: "슬레이트", solid: Color(hex: 0x8B94A5), soft: Color(hex: 0xE4E8EE))

    /// v2 태그 팔레트 전체 — 순서대로 8색
    static let tagPaletteV2: [TagV2] = [
        tagLavender, tagRose, tagSky, tagMint,
        tagAmber, tagCoral, tagOlive, tagSlate
    ]

    // MARK: - v2 그라데이션

    /// Primary gradient (보라) — p700 → p500
    static let primaryGradientV2 = LinearGradient(
        colors: [Color(hex: 0x4C2A8F), Color(hex: 0x8B5FBF)],
        startPoint: .topLeading,
        endPoint: .bottomTrailing
    )

    /// Accent gradient (재촉 CTA) — accent → accentDeep
    static let accentGradientV2 = LinearGradient(
        colors: [Color(hex: 0xFF5E7A), Color(hex: 0xE23D5E)],
        startPoint: .leading,
        endPoint: .trailing
    )

    // MARK: - v1 기존 토큰 (호환 유지 — 각 화면 마이그레이션 시 제거)

    // 오디 팔레트 v1
    static let odiya900 = Color(hex: 0x2D1B4E)
    static let odiya700 = Color(hex: 0x5B3A8C) // v1 Primary
    static let odiya500 = Color(hex: 0x8B5FBF) // v1 Secondary
    static let odiya300 = Color(hex: 0xC9A8E8) // v1 Tertiary
    static let odiya100 = Color(hex: 0xF0E6F7) // v1 Surface
    static let odiya50  = Color(hex: 0xF8F3FC) // v1 Background Tint

    // v1 기능별
    static let primary = odiya700
    static let secondary = odiya500
    static let nudge = Color(hex: 0xD94F8A)
    static let kakaoYellow = Color(hex: 0xFEE500)
    static let kakaoText = Color(hex: 0x3A1D1D)
    static let friendAccentBlue = Color(hex: 0x007AFF)

    // v1 iOS 시맨틱
    static let danger = Color(hex: 0xFF3B30)
    static let success = Color(hex: 0x34C759)
    static let warning = Color(hex: 0xFF9500)

    // v1 태그 프리셋 12색
    static let tagPresets: [Color] = [
        Color(hex: 0xFF3B30), Color(hex: 0xFF9500), Color(hex: 0xFFCC00), Color(hex: 0x34C759),
        Color(hex: 0x00C7BE), Color(hex: 0x5AC8FA), Color(hex: 0x007AFF), Color(hex: 0xAF52DE),
        Color(hex: 0xC9A8E8), Color(hex: 0xFF2D55), Color(hex: 0xA2845E), Color(hex: 0x8E8E93),
    ]

    // v1 태그 기본 색상
    static let tagFriend = Color(hex: 0x5AC8FA)
    static let tagLover  = Color(hex: 0xFF2D55)
    static let tagFamily = Color(hex: 0x34C759)

    // v1 그라데이션
    static let primaryGradient = LinearGradient(
        colors: [odiya700, odiya500],
        startPoint: .topLeading, endPoint: .bottomTrailing
    )
    static let nudgeGradient = LinearGradient(
        colors: [nudge, Color(hex: 0xE87BAD)],
        startPoint: .leading, endPoint: .trailing
    )
    static let loginBackgroundGradient = LinearGradient(
        colors: [Color(hex: 0xF8F3FC), Color(hex: 0xE8D5F5), Color(hex: 0xF0E6F7)],
        startPoint: .top, endPoint: .bottom
    )
    static let initialAvatarGradient = LinearGradient(
        colors: [odiya500, odiya300],
        startPoint: .topLeading, endPoint: .bottomTrailing
    )
}

// MARK: - Color Extensions

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

    /// 라이트/다크에 따라 hex 를 스위칭하는 동적 Color.
    init(lightHex: UInt, darkHex: UInt, opacity: Double = 1.0) {
        let ui = UIColor { trait in
            let hex = trait.userInterfaceStyle == .dark ? darkHex : lightHex
            return UIColor(
                red: CGFloat((hex >> 16) & 0xFF) / 255,
                green: CGFloat((hex >> 8) & 0xFF) / 255,
                blue: CGFloat(hex & 0xFF) / 255,
                alpha: CGFloat(opacity)
            )
        }
        self.init(ui)
    }

    /// 알파 등 복잡한 라이트/다크 색이 필요할 때 UIColor 를 직접 전달.
    init(lightUIColor: UIColor, darkUIColor: UIColor) {
        let ui = UIColor { trait in
            trait.userInterfaceStyle == .dark ? darkUIColor : lightUIColor
        }
        self.init(ui)
    }

    /// `Color.odiya.primary` 같은 네임스페이스 접근을 지원하기 위한 별칭.
    /// v2-design-renewal.md 의 명명 규약과 호출 감도를 맞춘다.
    static let odiya = OdiyaColors.self
}
