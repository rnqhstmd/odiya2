import SwiftUI

struct TagDotView: View {

    let color: Color
    var size: CGFloat = 10

    var body: some View {
        Circle()
            .fill(color)
            .frame(width: size, height: size)
    }
}

struct TagChipView: View {

    let tag: Tag
    let isSelected: Bool
    var action: (() -> Void)?

    var body: some View {
        Button(action: { action?() }) {
            HStack(spacing: 6) {
                TagDotView(color: tag.color, size: 8)
                Text(tag.name)
                    .font(.caption)
                    .fontWeight(isSelected ? .semibold : .regular)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isSelected ? tag.color.opacity(0.15) : Color(.systemGray6))
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(isSelected ? tag.color : Color.clear, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    HStack {
        TagDotView(color: OdiyaColors.tagLover)
        TagDotView(color: OdiyaColors.tagFriend)
        TagDotView(color: OdiyaColors.tagFamily)
        TagChipView(tag: .lover, isSelected: true)
        TagChipView(tag: .friend, isSelected: false)
    }
}
