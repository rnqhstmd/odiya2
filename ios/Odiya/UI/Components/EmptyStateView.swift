import SwiftUI

struct EmptyStateView: View {

    let iconName: String
    let title: String
    let description: String
    var actionTitle: String?
    var action: (() -> Void)?

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: iconName)
                .font(.system(size: 48))
                .foregroundStyle(OdiyaColors.odiya300)

            Text(title)
                .font(.headline)
                .foregroundStyle(.primary)

            Text(description)
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            if let actionTitle, let action {
                Button(action: action) {
                    Text(actionTitle)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(.white)
                        .padding(.horizontal, 24)
                        .padding(.vertical, 12)
                        .background(OdiyaColors.primary)
                        .clipShape(Capsule())
                }
                .padding(.top, 4)
            }
        }
        .padding(32)
    }
}

#Preview {
    EmptyStateView(
        iconName: "calendar.badge.plus",
        title: "다가오는 약속이 없어요",
        description: "새로운 약속을 만들어보세요",
        actionTitle: "약속 만들기",
        action: {}
    )
}
