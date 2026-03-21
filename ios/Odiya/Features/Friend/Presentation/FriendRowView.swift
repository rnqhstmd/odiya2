import SwiftUI

struct FriendRowView: View {

    let friend: Friend
    var onRemove: (() -> Void)?
    var onChangeTag: (() -> Void)?

    var body: some View {
        HStack(spacing: 12) {
            TagDotView(color: friend.tag.color, size: 10)

            ProfileImageView(imageUrl: friend.profileImageUrl, size: 40)

            Text(friend.nickname)
                .font(.body)
                .foregroundStyle(.primary)

            Spacer()

            Menu {
                Button {
                    onChangeTag?()
                } label: {
                    Label("태그 변경", systemImage: "tag")
                }

                Button(role: .destructive) {
                    onRemove?()
                } label: {
                    Label("친구 끊기", systemImage: "person.badge.minus")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(.secondary)
                    .frame(width: 36, height: 36)
                    .contentShape(Rectangle())
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .swipeActions(edge: .trailing, allowsFullSwipe: false) {
            Button(role: .destructive) {
                onRemove?()
            } label: {
                Label("끊기", systemImage: "person.badge.minus")
            }
        }
    }
}

#Preview {
    List {
        FriendRowView(
            friend: MockData.friends[0],
            onRemove: {},
            onChangeTag: {}
        )
        FriendRowView(
            friend: MockData.friends[1],
            onRemove: {},
            onChangeTag: {}
        )
    }
    .listStyle(.plain)
}
