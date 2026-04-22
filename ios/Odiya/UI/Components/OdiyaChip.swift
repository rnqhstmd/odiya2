import SwiftUI

/// v2 공용 칩.
///
/// `common-v2.jsx` 의 `Chip` 직역. pill 모양이고 크기 2종(sm/md) 과 색 모드 3가지(active/tagged/default)
/// 를 조합. tagged 모드에서는 좌측 도트에 태그 `solid`, 배경에 태그 `soft` 를 쓴다. active 는
/// 선택 상태(p700 배경).
struct OdiyaChip: View {

    enum Size {
        case sm, md

        var height: CGFloat {
            switch self {
            case .sm: return 28
            case .md: return 34
            }
        }

        var horizontalPadding: CGFloat {
            switch self {
            case .sm: return 10
            case .md: return 14
            }
        }

        var font: Font {
            switch self {
            case .sm: return OdiyaTypography.caption
            case .md: return OdiyaTypography.label
            }
        }

        var iconSize: CGFloat {
            switch self {
            case .sm: return 14
            case .md: return 16
            }
        }

        var dotSize: CGFloat {
            switch self {
            case .sm: return 6
            case .md: return 8
            }
        }

        var gap: CGFloat {
            switch self {
            case .sm: return 5
            case .md: return 6
            }
        }
    }

    let title: String
    var size: Size = .md
    /// 선택 상태
    var active: Bool = false
    /// 태그 색 (있으면 도트 + soft 배경)
    var tag: OdiyaColors.TagV2? = nil
    /// 우측 SF Symbol 아이콘 (선택)
    var icon: String? = nil
    var action: (() -> Void)? = nil

    var body: some View {
        let content = HStack(spacing: size.gap) {
            if let tag, !active {
                Circle()
                    .fill(tag.solid)
                    .frame(width: size.dotSize, height: size.dotSize)
            }
            if let icon {
                Image(systemName: icon)
                    .font(.system(size: size.iconSize, weight: .semibold))
            }
            Text(title)
                .font(size.font)
                .tracking(OdiyaTracking.body)
        }
        .frame(height: size.height)
        .padding(.horizontal, size.horizontalPadding)
        .foregroundStyle(foreground)
        .background(background)
        .clipShape(Capsule(style: .continuous))

        if let action {
            Button(action: action) { content }
                .buttonStyle(OdiyaPressableStyle())
        } else {
            content
        }
    }

    @ViewBuilder
    private var background: some View {
        if active {
            OdiyaColors.primaryV2
        } else if let tag {
            tag.soft
        } else {
            OdiyaColors.surface2
        }
    }

    private var foreground: Color {
        if active { return .white }
        if let tag { return tag.solid }
        return OdiyaColors.textPrimary
    }
}

#if DEBUG
#Preview("Light") {
    ScrollView {
        VStack(alignment: .leading, spacing: 20) {
            HStack(spacing: 8) {
                OdiyaChip(title: "전체", active: true)
                OdiyaChip(title: "친구 (3)", tag: OdiyaColors.tagSky)
                OdiyaChip(title: "연인 (1)", tag: OdiyaColors.tagRose)
                OdiyaChip(title: "가족 (1)", tag: OdiyaColors.tagMint)
            }

            HStack(spacing: 6) {
                OdiyaChip(title: "today", size: .sm)
                OdiyaChip(title: "추천", size: .sm, active: true)
                OdiyaChip(title: "라벤더", size: .sm, tag: OdiyaColors.tagLavender)
            }

            HStack(spacing: 6) {
                OdiyaChip(title: "검색", icon: "magnifyingglass")
                OdiyaChip(title: "필터", icon: "line.3.horizontal.decrease")
            }
        }
        .padding(24)
    }
    .background(OdiyaColors.background)
}

#Preview("Dark") {
    VStack(alignment: .leading, spacing: 16) {
        HStack(spacing: 8) {
            OdiyaChip(title: "전체", active: true)
            OdiyaChip(title: "친구", tag: OdiyaColors.tagSky)
            OdiyaChip(title: "연인", tag: OdiyaColors.tagRose)
        }
        HStack(spacing: 6) {
            OdiyaChip(title: "작게", size: .sm)
            OdiyaChip(title: "선택", size: .sm, active: true)
        }
    }
    .padding(24)
    .background(OdiyaColors.background)
    .preferredColorScheme(.dark)
}
#endif
