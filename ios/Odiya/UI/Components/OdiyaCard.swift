import SwiftUI

/// v2 공용 카드 컨테이너.
///
/// `common-v2.jsx` 의 `Card` 직역. 기본 radius=xxl(28), padding=20, `.card` 섀도.
/// 좌측 태그 컬러바(`tag`) 를 옵션으로 넣을 수 있어 약속 카드의 태그 컬러 전파 패턴을
/// 그대로 구현한다.
struct OdiyaCard<CardBody: View>: View {

    var padding: CGFloat = 20
    var radius: CGFloat = OdiyaRadiusV2.xxl
    var elevated: Bool = true
    var tag: Color? = nil
    @ViewBuilder let content: () -> CardBody

    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        let base = content()
            .padding(padding)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                OdiyaColors.surface
                    .clipShape(RoundedRectangle(cornerRadius: radius, style: .continuous))
            )
            .overlay(alignment: .leading) {
                if let tag {
                    tag
                        .frame(width: 4)
                        .padding(.vertical, 16)
                        .clipShape(RoundedRectangle(cornerRadius: 1.5, style: .continuous))
                }
            }

        if elevated {
            base.odiyaShadow(.card)
        } else {
            base
        }
    }
}

/// 섹션 사이에 두는 작은 헤더 라벨.
/// `common-v2.jsx` 의 `SectionLabel` 직역.
struct OdiyaSectionLabel: View {
    let title: String
    var action: (() -> Void)? = nil
    var actionLabel: String? = nil

    var body: some View {
        HStack {
            Text(title)
                .font(OdiyaTypography.label)
                .foregroundStyle(OdiyaColors.textSecondary)
                .tracking(OdiyaTracking.body)
            Spacer()
            if let action, let actionLabel {
                Button(actionLabel, action: action)
                    .font(OdiyaTypography.label)
                    .foregroundStyle(OdiyaColors.interactive)
            }
        }
        .padding(.horizontal, 24)
        .padding(.top, 20)
        .padding(.bottom, 8)
    }
}

#if DEBUG
#Preview("Light") {
    ScrollView {
        VStack(alignment: .leading, spacing: 16) {
            OdiyaSectionLabel(title: "다가오는 약속")

            OdiyaCard(tag: OdiyaColors.tagLavender.solid) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("점심 약속")
                        .font(OdiyaTypography.sectionHeader)
                        .foregroundStyle(OdiyaColors.textPrimary)
                    Text("14:30 · 성수동 카페")
                        .font(OdiyaTypography.bodySm)
                        .foregroundStyle(OdiyaColors.textSecondary)
                }
            }
            .padding(.horizontal, 24)

            OdiyaCard(tag: OdiyaColors.tagMint.solid, elevated: false) {
                Text("비-엘리베이티드 + 민트 태그")
                    .font(OdiyaTypography.body)
                    .foregroundStyle(OdiyaColors.textPrimary)
            }
            .padding(.horizontal, 24)
        }
        .padding(.vertical, 24)
    }
    .background(OdiyaColors.background)
}

#Preview("Dark") {
    ScrollView {
        VStack(spacing: 16) {
            OdiyaSectionLabel(title: "다가오는 약속", action: {}, actionLabel: "전체")

            OdiyaCard(tag: OdiyaColors.tagRose.solid) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("저녁 약속")
                        .font(OdiyaTypography.sectionHeader)
                        .foregroundStyle(OdiyaColors.textPrimary)
                    Text("19:00 · 홍대 와인바")
                        .font(OdiyaTypography.bodySm)
                        .foregroundStyle(OdiyaColors.textSecondary)
                }
            }
            .padding(.horizontal, 24)
        }
        .padding(.vertical, 24)
    }
    .background(OdiyaColors.background)
    .preferredColorScheme(.dark)
}
#endif
