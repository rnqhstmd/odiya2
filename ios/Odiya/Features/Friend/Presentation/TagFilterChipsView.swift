import SwiftUI

struct TagFilterChipsView: View {

    let availableTags: [Tag]
    @Binding var selectedTag: Tag?

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                // "전체" 칩
                allChip

                // 태그별 칩
                ForEach(availableTags) { tag in
                    TagChipView(tag: tag, isSelected: selectedTag == tag) {
                        selectedTag = tag
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)
        }
    }

    private var allChip: some View {
        let allTag = Tag(id: 0, name: "전체", colorHex: "#5B3A8C", isDefault: true)
        return Button {
            selectedTag = nil
        } label: {
            HStack(spacing: 6) {
                TagDotView(color: OdiyaColors.primary, size: 8)
                Text("전체")
                    .font(.caption)
                    .fontWeight(selectedTag == nil ? .semibold : .regular)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(selectedTag == nil ? OdiyaColors.primary.opacity(0.15) : Color(.systemGray6))
            .clipShape(Capsule())
            .overlay(
                Capsule()
                    .stroke(selectedTag == nil ? OdiyaColors.primary : Color.clear, lineWidth: 1.5)
            )
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    @Previewable @State var selectedTag: Tag? = nil
    TagFilterChipsView(availableTags: MockData.tags, selectedTag: $selectedTag)
}
