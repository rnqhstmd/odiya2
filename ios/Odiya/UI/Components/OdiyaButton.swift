import SwiftUI

/// v2 공용 버튼.
///
/// `tokens-v2.jsx` 의 `Btn` 컴포넌트 직역. variant 4종 × size 3종 조합을 지원하고,
/// 좌측 아이콘 · 우측 trailing 아이콘 · 전체 폭 옵션을 갖는다. 다크모드는 `OdiyaColors`
/// 의 시맨틱 토큰을 통해 자동 해석된다.
struct OdiyaButton: View {

    enum Variant {
        /// 메인 CTA — p700 배경, 흰 텍스트
        case primary
        /// 보조 — 회색 배경, 기본 텍스트
        case secondary
        /// 투명 배경 — 텍스트만
        case ghost
        /// 재촉 / 긴급 — accent + glow
        case accent
    }

    enum Size {
        case sm, md, lg

        var height: CGFloat {
            switch self {
            case .sm: return 36
            case .md: return 48
            case .lg: return 56
            }
        }

        var horizontalPadding: CGFloat {
            switch self {
            case .sm: return 14
            case .md: return 18
            case .lg: return 22
            }
        }

        var font: Font {
            switch self {
            case .sm: return OdiyaTypography.buttonSm
            case .md: return OdiyaTypography.buttonMd
            case .lg: return OdiyaTypography.buttonLg
            }
        }

        var radius: CGFloat {
            switch self {
            case .sm, .md: return OdiyaRadiusV2.md
            case .lg: return OdiyaRadiusV2.lg
            }
        }

        var iconSize: CGFloat {
            switch self {
            case .sm: return 16
            case .md: return 18
            case .lg: return 20
            }
        }
    }

    let title: String
    var variant: Variant = .primary
    var size: Size = .md
    var icon: String? = nil
    var trailing: String? = nil
    var fullWidth: Bool = false
    var isEnabled: Bool = true
    let action: () -> Void

    @Environment(\.colorScheme) private var colorScheme
    private var isDark: Bool { colorScheme == .dark }

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if let icon {
                    Image(systemName: icon)
                        .font(.system(size: size.iconSize, weight: .semibold))
                }
                Text(title)
                    .font(size.font)
                    .tracking(OdiyaTracking.button)
                if let trailing {
                    Image(systemName: trailing)
                        .font(.system(size: size.iconSize, weight: .semibold))
                }
            }
            .frame(maxWidth: fullWidth ? .infinity : nil)
            .frame(height: size.height)
            .padding(.horizontal, fullWidth ? 0 : size.horizontalPadding)
            .foregroundStyle(foreground)
            .background(background)
            .clipShape(RoundedRectangle(cornerRadius: size.radius, style: .continuous))
            .modifier(ShadowModifier(variant: variant))
            .opacity(isEnabled ? 1.0 : 0.5)
        }
        .disabled(!isEnabled)
        .buttonStyle(.plain)
    }

    private var foreground: Color {
        switch variant {
        case .primary, .accent:
            return .white
        case .secondary, .ghost:
            return OdiyaColors.textPrimary
        }
    }

    @ViewBuilder
    private var background: some View {
        switch variant {
        case .primary:
            OdiyaColors.primaryV2
        case .secondary:
            OdiyaColors.surface2
        case .ghost:
            Color.clear
        case .accent:
            OdiyaColors.accent
        }
    }

    private struct ShadowModifier: ViewModifier {
        let variant: Variant

        func body(content: Content) -> some View {
            switch variant {
            case .primary:
                content.shadow(color: OdiyaColors.p700.opacity(0.25), radius: 10, x: 0, y: 3)
            case .accent:
                content.odiyaShadow(.glow)
            case .secondary, .ghost:
                content
            }
        }
    }
}

#if DEBUG
#Preview("Light") {
    ScrollView {
        VStack(spacing: 16) {
            Group {
                OdiyaButton(title: "Primary LG", size: .lg, fullWidth: true) {}
                OdiyaButton(title: "Primary MD") {}
                OdiyaButton(title: "Primary SM", size: .sm) {}
                OdiyaButton(title: "아이콘", icon: "plus") {}
                OdiyaButton(title: "보조", variant: .secondary) {}
                OdiyaButton(title: "고스트", variant: .ghost) {}
                OdiyaButton(title: "재촉하기", variant: .accent, size: .lg, icon: "bolt.fill", fullWidth: true) {}
                OdiyaButton(title: "비활성", isEnabled: false) {}
            }
        }
        .padding(24)
    }
    .background(OdiyaColors.background)
}

#Preview("Dark") {
    ScrollView {
        VStack(spacing: 16) {
            OdiyaButton(title: "Primary LG", size: .lg, fullWidth: true) {}
            OdiyaButton(title: "Primary MD") {}
            OdiyaButton(title: "보조", variant: .secondary) {}
            OdiyaButton(title: "재촉하기", variant: .accent, size: .lg, icon: "bolt.fill", fullWidth: true) {}
        }
        .padding(24)
    }
    .background(OdiyaColors.background)
    .preferredColorScheme(.dark)
}
#endif
