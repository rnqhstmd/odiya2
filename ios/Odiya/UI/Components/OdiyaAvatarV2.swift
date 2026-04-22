import SwiftUI

/// v2 아바타.
///
/// `common-v2.jsx` 의 `Ava` / `AvaStack` 직역. 태그 색이 있으면 해당 색의 그라데이션을,
/// 없으면 이름 해시로 태그 팔레트에서 자동 선택한 색을 쓴다. v1 `ProfileImageView`와
/// 병존하며, v2 화면이 v1 을 교체할 때 이 컴포넌트를 쓴다.
struct OdiyaAvatarV2: View {

    let name: String
    var size: CGFloat = 36
    var tag: OdiyaColors.TagV2? = nil
    var bordered: Bool = false

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let baseColor = resolvedColor
        ZStack {
            LinearGradient(
                colors: [baseColor, baseColor.opacity(0.8)],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            Text(String(name.prefix(1)))
                .font(.system(size: size * 0.4, weight: .bold, design: .default))
                .foregroundStyle(.white)
                .tracking(-0.3)
        }
        .frame(width: size, height: size)
        .clipShape(Circle())
        .overlay {
            if bordered {
                Circle()
                    .stroke(OdiyaColors.surface, lineWidth: 2)
            }
        }
    }

    private var resolvedColor: Color {
        if let tag { return tag.solid }
        // 이름 해시로 팔레트에서 안정적 색 선택
        let hash = name.unicodeScalars.reduce(0) { $0 + Int($1.value) }
        let palette = OdiyaColors.tagPaletteV2.map(\.solid)
        return palette[hash % palette.count]
    }
}

/// v2 아바타 스택 — 겹치게 나열하고 초과 인원은 "+N" 버블.
struct OdiyaAvatarStackV2: View {

    let names: [String]
    var size: CGFloat = 28
    var maxDisplay: Int = 4
    /// 선택적 태그 매핑 (names[i] → tag). nil 이면 이름 해시 기반 색.
    var tags: [OdiyaColors.TagV2?]? = nil

    var body: some View {
        let shown = Array(names.prefix(maxDisplay))
        let rest = max(0, names.count - maxDisplay)
        HStack(spacing: -8) {
            ForEach(Array(shown.enumerated()), id: \.offset) { index, name in
                OdiyaAvatarV2(
                    name: name,
                    size: size,
                    tag: tags?[safe: index] ?? nil,
                    bordered: true
                )
                .zIndex(Double(maxDisplay - index))
            }
            if rest > 0 {
                Text("+\(rest)")
                    .font(.system(size: size * 0.34, weight: .bold).monospacedDigit())
                    .foregroundStyle(OdiyaColors.textSecondary)
                    .frame(width: size, height: size)
                    .background(OdiyaColors.surface2)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(OdiyaColors.surface, lineWidth: 2))
            }
        }
    }
}

// MARK: - Safe subscript helper

private extension Array {
    subscript(safe index: Int) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}

#if DEBUG
#Preview("Light") {
    VStack(spacing: 24) {
        HStack(spacing: 12) {
            OdiyaAvatarV2(name: "민지", size: 56)
            OdiyaAvatarV2(name: "서준", size: 56, tag: OdiyaColors.tagSky)
            OdiyaAvatarV2(name: "하늘", size: 56, tag: OdiyaColors.tagMint)
            OdiyaAvatarV2(name: "우진", size: 56, tag: OdiyaColors.tagAmber)
        }
        OdiyaAvatarStackV2(names: ["민지", "서준", "하늘", "우진", "유나", "지호"])
        OdiyaAvatarStackV2(
            names: ["민지", "서준", "하늘"],
            size: 40,
            tags: [OdiyaColors.tagSky, OdiyaColors.tagRose, OdiyaColors.tagMint]
        )
    }
    .padding(24)
    .background(OdiyaColors.background)
}

#Preview("Dark") {
    VStack(spacing: 24) {
        HStack(spacing: 12) {
            OdiyaAvatarV2(name: "민지", size: 56, tag: OdiyaColors.tagRose)
            OdiyaAvatarV2(name: "서준", size: 56, tag: OdiyaColors.tagSky)
        }
        OdiyaAvatarStackV2(names: ["민지", "서준", "하늘", "우진", "유나"])
    }
    .padding(24)
    .background(OdiyaColors.background)
    .preferredColorScheme(.dark)
}
#endif
