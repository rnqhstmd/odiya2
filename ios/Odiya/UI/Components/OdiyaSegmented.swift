import SwiftUI

/// v2 세그먼티드 컨트롤.
///
/// `common-v2.jsx` 의 `Seg` 직역. 토스 스타일 — 컨테이너는 낮은 레이어(라이트=`surface2`,
/// 다크=반투명 화이트 오버레이)로 깔고, 선택된 탭이 그 위에 "올라간" 느낌이 되도록
/// 라이트=`surface`(흰색), 다크=`surface2`(살짝 밝은 회색)로 올린다. `OdiyaColors.surface`
/// 는 다크에서 오히려 더 어둡기 때문에 컨테이너·선택 배경을 `colorScheme` 기반으로
/// 명시적으로 분기한다. Generic `Value` 는 Hashable 을 만족하는 모든 타입(Enum 포함).
struct OdiyaSegmented<Value: Hashable>: View {

    struct Option: Identifiable {
        let id: Value
        let label: String
        init(_ value: Value, label: String) {
            self.id = value
            self.label = label
        }
    }

    let options: [Option]
    @Binding var selection: Value

    @Environment(\.colorScheme) private var colorScheme
    @Namespace private var segNamespace

    private var isDark: Bool { colorScheme == .dark }

    /// 컨테이너 배경 — 낮은 레이어.
    /// light: `surface2` (`g100 = #F4F4F7`) · dark: 화이트 오버레이(6%) — page 위에 얹혀 매우 어둡게 보임
    private var containerBackground: Color {
        isDark ? Color.white.opacity(0.06) : OdiyaColors.surface2
    }

    /// 선택된 탭 배경 — 컨테이너 위에 "올라간" 레이어.
    /// light: `surface` (#FFFFFF) · dark: `surface2` (`0x1F1F28` — 컨테이너보다 밝음)
    private var selectedBackground: Color {
        isDark ? OdiyaColors.surface2 : OdiyaColors.surface
    }

    var body: some View {
        HStack(spacing: 2) {
            ForEach(options) { option in
                let isOn = option.id == selection
                Button {
                    withAnimation(.spring(response: 0.28, dampingFraction: 0.85)) {
                        selection = option.id
                    }
                } label: {
                    Text(option.label)
                        .font(OdiyaTypography.bodySm)
                        .tracking(OdiyaTracking.body)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 9)
                        .foregroundStyle(isOn ? OdiyaColors.textPrimary : OdiyaColors.textSecondary)
                        .background {
                            if isOn {
                                RoundedRectangle(cornerRadius: OdiyaRadiusV2.sm, style: .continuous)
                                    .fill(selectedBackground)
                                    .shadow(color: .black.opacity(isDark ? 0 : 0.06), radius: 6, x: 0, y: 2)
                                    .matchedGeometryEffect(id: "seg", in: segNamespace)
                            }
                        }
                }
                .buttonStyle(OdiyaPressableStyle())
            }
        }
        .padding(3)
        .background(
            RoundedRectangle(cornerRadius: OdiyaRadiusV2.md, style: .continuous)
                .fill(containerBackground)
        )
    }
}

#if DEBUG
private enum PreviewTab: String, CaseIterable {
    case upcoming, past
    var label: String {
        switch self {
        case .upcoming: return "다가오는 약속"
        case .past: return "지난 약속"
        }
    }
}

private struct PreviewHost: View {
    @State var sel: PreviewTab = .upcoming
    var body: some View {
        VStack(spacing: 24) {
            OdiyaSegmented(
                options: PreviewTab.allCases.map { OdiyaSegmented.Option($0, label: $0.label) },
                selection: $sel
            )
            .padding(.horizontal, 24)
            Text("Selected: \(sel.rawValue)")
                .font(OdiyaTypography.body)
                .foregroundStyle(OdiyaColors.textSecondary)
        }
    }
}

#Preview("Light") {
    PreviewHost()
        .padding(.vertical, 24)
        .background(OdiyaColors.background)
}

#Preview("Dark") {
    PreviewHost()
        .padding(.vertical, 24)
        .background(OdiyaColors.background)
        .preferredColorScheme(.dark)
}
#endif
