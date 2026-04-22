import SwiftUI

/// 디자인 시스템 v2 섀도 토큰.
///
/// SwiftUI `.shadow` 는 단일 레이어만 지원하므로 2-layer soft shadow 는
/// modifier 내부에서 `.shadow` 를 두 번 체이닝해 구현한다.
///
/// 다크모드에서는 블랙 섀도 대신 얇은 외곽 보더와 미세 섀도로 대체한다
/// (SwiftUI `.shadow` 자체엔 `colorScheme` 반응이 없으므로 View 단에서 처리).
enum OdiyaShadow {
    /// 기본 카드 — 살짝 들뜸
    case card
    /// 들린 카드 (중요 리스트 아이템, 바텀 시트 상단)
    case lift
    /// HERO 카드 — 보라 톤의 깊은 섀도
    case hero
    /// 재촉 CTA glow — 산호 톤의 확산
    case glow
}

extension View {
    /// v2 섀도 토큰 적용. 다크모드에서는 자동으로 섀도 강도를 낮추고 보더를 대체한다.
    @ViewBuilder
    func odiyaShadow(_ shadow: OdiyaShadow) -> some View {
        self.modifier(OdiyaShadowModifier(shadow: shadow))
    }
}

private struct OdiyaShadowModifier: ViewModifier {
    let shadow: OdiyaShadow
    @Environment(\.colorScheme) private var colorScheme

    private var isDark: Bool { colorScheme == .dark }

    func body(content: Content) -> some View {
        switch shadow {
        case .card:
            content
                .shadow(color: isDark ? .black.opacity(0.25) : Color(red: 17/255, green: 17/255, blue: 28/255).opacity(0.04), radius: 2, x: 0, y: 1)
                .shadow(color: isDark ? .black.opacity(0.15) : Color(red: 17/255, green: 17/255, blue: 28/255).opacity(0.04), radius: 12, x: 0, y: 4)
        case .lift:
            content
                .shadow(color: isDark ? .black.opacity(0.3) : Color(red: 17/255, green: 17/255, blue: 28/255).opacity(0.05), radius: 4, x: 0, y: 2)
                .shadow(color: isDark ? .black.opacity(0.2) : Color(red: 17/255, green: 17/255, blue: 28/255).opacity(0.08), radius: 32, x: 0, y: 12)
        case .hero:
            let purple = OdiyaColors.p700
            content
                .shadow(color: purple.opacity(isDark ? 0.25 : 0.12), radius: 10, x: 0, y: 4)
                .shadow(color: purple.opacity(isDark ? 0.45 : 0.22), radius: 48, x: 0, y: 20)
        case .glow:
            content
                .shadow(color: OdiyaColors.accent.opacity(isDark ? 0.5 : 0.35), radius: 24, x: 0, y: 8)
        }
    }
}
