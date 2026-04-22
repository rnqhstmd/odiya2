import SwiftUI

/// v2 세그먼티드 컨트롤.
///
/// `common-v2.jsx` 의 `Seg` 직역. 토스 스타일 — 컨테이너는 `surface2`, 선택된 탭은
/// `surface` + subtle shadow (라이트) / `surface2` 강조 (다크). Generic `Value` 는
/// Hashable & Equatable & Identifiable 을 만족하는 모든 타입 (Enum 포함).
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
                                    .fill(OdiyaColors.surface)
                                    .shadow(color: .black.opacity(colorScheme == .dark ? 0 : 0.06), radius: 6, x: 0, y: 2)
                                    .matchedGeometryEffect(id: "seg", in: segNamespace)
                            }
                        }
                }
                .buttonStyle(.plain)
            }
        }
        .padding(3)
        .background(
            RoundedRectangle(cornerRadius: OdiyaRadiusV2.md, style: .continuous)
                .fill(OdiyaColors.surface2)
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
