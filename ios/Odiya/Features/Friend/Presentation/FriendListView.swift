import SwiftUI

struct FriendListView: View {

    @StateObject private var viewModel = FriendListViewModel()
    @State private var showAddFriend = false
    @State private var showTagChangePicker = false
    @State private var tagChangeTarget: Friend? = nil
    @Environment(\.showNotifications) private var showNotifications
    @Environment(\.showProfile) private var showProfile

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                TagFilterChipsView(
                    availableTags: viewModel.availableTags,
                    selectedTag: $viewModel.selectedTag
                )

                Divider()

                if viewModel.isLoading {
                    Spacer()
                    ProgressView()
                    Spacer()
                } else if viewModel.filteredFriends.isEmpty {
                    Spacer()
                    EmptyStateView(
                        iconName: "person.2.slash",
                        title: "친구가 없어요",
                        description: "오디야에 친구를 초대해보세요",
                        actionTitle: "친구 추가",
                        action: { showAddFriend = true }
                    )
                    Spacer()
                } else {
                    List {
                        ForEach(viewModel.filteredFriends) { friend in
                            FriendRowView(
                                friend: friend,
                                onRemove: {
                                    viewModel.confirmEndFriendship(friend)
                                },
                                onChangeTag: {
                                    tagChangeTarget = friend
                                    showTagChangePicker = true
                                }
                            )
                            .listRowInsets(EdgeInsets())
                            .listRowSeparator(.hidden)
                        }
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("친구")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    HStack(spacing: 4) {
                        Button { showAddFriend = true } label: {
                            Image(systemName: "plus")
                        }
                        Button { showNotifications.wrappedValue = true } label: {
                            Image(systemName: "bell")
                        }
                        Button { showProfile.wrappedValue = true } label: {
                            Image(systemName: "person.circle")
                        }
                    }
                    .tint(OdiyaColors.primary)
                }
            }
            .searchable(
                text: $viewModel.searchText,
                placement: .navigationBarDrawer(displayMode: .always),
                prompt: "친구 이름 검색"
            )
            .onAppear {
                Task { await viewModel.loadFriends() }
            }
            .sheet(isPresented: $showAddFriend) {
                AddFriendView()
            }
            .alert("친구 끊기", isPresented: $viewModel.showEndFriendshipAlert) {
                Button("끊기", role: .destructive) { viewModel.endFriendship() }
                Button("취소", role: .cancel) {}
            } message: {
                if let target = viewModel.endFriendshipTarget {
                    Text("\(target.nickname)님과의 친구를 끊으시겠어요? 서로 약속에 초대할 수 없게 됩니다.")
                }
            }
            .confirmationDialog(
                "태그 변경",
                isPresented: $showTagChangePicker,
                titleVisibility: .visible
            ) {
                ForEach(viewModel.availableTags) { tag in
                    Button(tag.name) {
                        if let target = tagChangeTarget {
                            viewModel.changeTag(friendId: target.id, tag: tag)
                        }
                    }
                }
                Button("취소", role: .cancel) {}
            } message: {
                if let target = tagChangeTarget {
                    Text("\(target.nickname)의 태그를 변경합니다")
                }
            }
        }
    }
}

#Preview {
    FriendListView()
}
