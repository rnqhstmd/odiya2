import SwiftUI

struct Step3ParticipantsView: View {

    @ObservedObject var viewModel: CreateAppointmentViewModel

    var body: some View {
        VStack(spacing: 0) {
            // MARK: - 검색바
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(.secondary)
                TextField("친구 검색", text: $viewModel.friendSearchText)
                    .font(.subheadline)
                if !viewModel.friendSearchText.isEmpty {
                    Button {
                        viewModel.friendSearchText = ""
                    } label: {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundStyle(.secondary)
                    }
                }
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal, 20)
            .padding(.top, 16)

            // MARK: - 선택 칩
            if !viewModel.selectedFriends.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(Array(viewModel.selectedFriends).sorted(by: { $0.id < $1.id })) { friend in
                            SelectedFriendChip(friend: friend) {
                                viewModel.toggleFriend(friend)
                            }
                        }
                    }
                    .padding(.horizontal, 20)
                }
                .padding(.vertical, 10)
            }

            Divider()
                .padding(.top, viewModel.selectedFriends.isEmpty ? 12 : 0)

            // MARK: - 친구 리스트
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(viewModel.availableFriends) { friend in
                        FriendSelectRow(
                            friend: friend,
                            isSelected: viewModel.selectedFriends.contains(friend)
                        ) {
                            viewModel.toggleFriend(friend)
                        }

                        if friend.id != viewModel.availableFriends.last?.id {
                            Divider().padding(.leading, 68)
                        }
                    }
                }
            }

            // MARK: - 하단 카운트
            Divider()
            HStack {
                Image(systemName: "person.2.fill")
                    .foregroundStyle(OdiyaColors.primary)
                Text("선택: ")
                    .foregroundColor(.secondary)
                + Text("\(viewModel.selectedFriends.count)명")
                    .foregroundColor(OdiyaColors.primary)
                    .fontWeight(.semibold)
                Spacer()
            }
            .font(.subheadline)
            .padding(.horizontal, 20)
            .padding(.vertical, 14)
            .background(Color(.systemBackground))
        }
    }
}

// MARK: - SelectedFriendChip

private struct SelectedFriendChip: View {
    let friend: Friend
    let onRemove: () -> Void

    var body: some View {
        HStack(spacing: 6) {
            ProfileImageView(imageUrl: friend.profileImageUrl, size: 24)

            Text(friend.nickname)
                .font(.subheadline)
                .fontWeight(.medium)

            Button(action: onRemove) {
                Image(systemName: "xmark")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundStyle(.secondary)
            }
        }
        .padding(.leading, 4)
        .padding(.trailing, 10)
        .padding(.vertical, 6)
        .background(OdiyaColors.odiya100)
        .clipShape(Capsule())
    }
}

// MARK: - FriendSelectRow

private struct FriendSelectRow: View {
    let friend: Friend
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 14) {
                ZStack {
                    Circle()
                        .fill(friend.tag.color.opacity(0.2))
                        .frame(width: 40, height: 40)
                    Text(String(friend.nickname.prefix(1)))
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundStyle(friend.tag.color)
                }

                VStack(alignment: .leading, spacing: 2) {
                    Text(friend.nickname)
                        .font(.subheadline)
                        .fontWeight(isSelected ? .semibold : .regular)
                        .foregroundStyle(.primary)

                    HStack(spacing: 4) {
                        TagDotView(color: friend.tag.color, size: 7)
                        Text(friend.tag.name)
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }

                Spacer()

                if isSelected {
                    Image(systemName: "checkmark.circle.fill")
                        .foregroundStyle(OdiyaColors.primary)
                        .font(.system(size: 20))
                } else {
                    Image(systemName: "circle")
                        .foregroundStyle(Color(.systemGray4))
                        .font(.system(size: 20))
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
            .background(isSelected ? OdiyaColors.odiya50 : Color(.systemBackground))
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .animation(.easeInOut(duration: 0.15), value: isSelected)
    }
}

#Preview {
    Step3ParticipantsView(viewModel: CreateAppointmentViewModel())
}
