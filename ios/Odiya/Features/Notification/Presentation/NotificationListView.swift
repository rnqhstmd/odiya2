import SwiftUI

struct NotificationListView: View {

    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = NotificationListViewModel()

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.notifications.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if viewModel.notifications.isEmpty {
                    EmptyStateView(
                        iconName: "bell.slash",
                        title: "알림이 없어요",
                        description: "새로운 알림이 오면 여기에 표시돼요"
                    )
                } else {
                    List {
                        ForEach(viewModel.notifications) { notification in
                            notificationRow(notification)
                                .listRowInsets(EdgeInsets())
                                .listRowSeparator(.hidden)
                                .listRowBackground(
                                    notification.isRead
                                        ? Color(.systemBackground)
                                        : OdiyaColors.odiya50
                                )
                                .onTapGesture {
                                    Task { await viewModel.markAsRead(id: notification.id) }
                                }
                                .onAppear {
                                    if notification.id == viewModel.notifications.last?.id {
                                        Task { await viewModel.loadMoreIfNeeded() }
                                    }
                                }
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("알림")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    if viewModel.unreadCount > 0 {
                        Button("전체 읽음") {
                            Task { await viewModel.markAllAsRead() }
                        }
                        .tint(OdiyaColors.primary)
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("닫기") {
                        dismiss()
                    }
                    .tint(OdiyaColors.primary)
                }
            }
            .task {
                await viewModel.loadNotifications()
            }
        }
    }

    // MARK: - Notification Row

    @ViewBuilder
    private func notificationRow(_ notification: AppNotification) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: notification.type.iconName)
                    .font(.system(size: 20))
                    .foregroundColor(OdiyaColors.primary)
                    .frame(width: 36, height: 36)
                    .background(OdiyaColors.odiya100)
                    .clipShape(Circle())

                VStack(alignment: .leading, spacing: 4) {
                    Text(notification.title)
                        .font(.subheadline)
                        .fontWeight(notification.isRead ? .regular : .semibold)
                        .foregroundColor(.primary)

                    Text(notification.body)
                        .font(.caption)
                        .foregroundColor(.secondary)
                        .lineLimit(2)

                    Text(notification.timeAgoText)
                        .font(.caption2)
                        .foregroundColor(OdiyaColors.odiya300)

                    if notification.type == .friendRequest {
                        friendRequestButtons(notification)
                            .padding(.top, 6)
                    }
                }

                Spacer()

                if !notification.isRead {
                    Circle()
                        .fill(OdiyaColors.primary)
                        .frame(width: 8, height: 8)
                        .padding(.top, 4)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)

            Divider()
                .padding(.leading, 64)
        }
    }

    // MARK: - Friend Request Buttons

    private func friendRequestButtons(_ notification: AppNotification) -> some View {
        HStack(spacing: 8) {
            Button {
                viewModel.acceptFriendRequest(id: notification.id)
            } label: {
                Text("수락")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(.white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 6)
                    .background(OdiyaColors.primary)
                    .clipShape(Capsule())
            }

            Button {
                viewModel.declineFriendRequest(id: notification.id)
            } label: {
                Text("거절")
                    .font(.caption)
                    .fontWeight(.semibold)
                    .foregroundColor(OdiyaColors.primary)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 6)
                    .background(OdiyaColors.odiya100)
                    .clipShape(Capsule())
            }
        }
    }
}

#Preview {
    NotificationListView()
}
