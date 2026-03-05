import SwiftUI

struct AddFriendView: View {

    @Environment(\.dismiss) private var dismiss
    @StateObject private var viewModel = AddFriendViewModel()

    var body: some View {
        NavigationStack {
            List {
                // MARK: - 받은 요청
                if !viewModel.pendingRequests.isEmpty {
                    Section {
                        ForEach(viewModel.pendingRequests) { friend in
                            HStack(spacing: 12) {
                                ProfileImageView(imageUrl: friend.profileImageUrl, size: 44)

                                Text(friend.nickname)
                                    .font(.body)

                                Spacer()

                                Button {
                                    viewModel.acceptRequest(friend)
                                } label: {
                                    Text("수락")
                                        .font(.subheadline)
                                        .fontWeight(.semibold)
                                        .foregroundStyle(.white)
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 7)
                                        .background(OdiyaColors.primary)
                                        .clipShape(Capsule())
                                }
                                .buttonStyle(.plain)

                                Button {
                                    viewModel.rejectRequest(friend)
                                } label: {
                                    Text("거절")
                                        .font(.subheadline)
                                        .fontWeight(.medium)
                                        .foregroundStyle(.secondary)
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 7)
                                        .background(Color(.systemGray5))
                                        .clipShape(Capsule())
                                }
                                .buttonStyle(.plain)
                            }
                            .padding(.vertical, 4)
                        }
                    } header: {
                        Label("받은 요청", systemImage: "person.badge.clock")
                    }
                }

                // MARK: - 친구 검색
                Section {
                    if viewModel.filteredSearchResults.isEmpty && !viewModel.searchText.isEmpty {
                        HStack {
                            Spacer()
                            Text("검색 결과가 없어요")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                            Spacer()
                        }
                        .padding(.vertical, 20)
                    } else {
                        ForEach(viewModel.filteredSearchResults) { user in
                            HStack(spacing: 12) {
                                ProfileImageView(imageUrl: user.profileImageUrl, size: 44)

                                Text(user.nickname)
                                    .font(.body)

                                Spacer()

                                if viewModel.sentRequestIds.contains(user.id) {
                                    Text("요청됨")
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                        .padding(.horizontal, 14)
                                        .padding(.vertical, 7)
                                        .background(Color(.systemGray5))
                                        .clipShape(Capsule())
                                } else {
                                    Button {
                                        viewModel.sendFriendRequest(to: user)
                                    } label: {
                                        Text("요청")
                                            .font(.subheadline)
                                            .fontWeight(.semibold)
                                            .foregroundStyle(.white)
                                            .padding(.horizontal, 14)
                                            .padding(.vertical, 7)
                                            .background(OdiyaColors.primary)
                                            .clipShape(Capsule())
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                    }
                } header: {
                    Label("친구 검색", systemImage: "magnifyingglass")
                }
            }
            .navigationTitle("친구 추가")
            .navigationBarTitleDisplayMode(.inline)
            .searchable(
                text: $viewModel.searchText,
                placement: .navigationBarDrawer(displayMode: .always),
                prompt: "닉네임으로 검색"
            )
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("닫기") { dismiss() }
                }
            }
            .alert("알림", isPresented: $viewModel.showAlert) {
                Button("확인", role: .cancel) {}
            } message: {
                Text(viewModel.alertMessage)
            }
        }
    }
}

#Preview {
    AddFriendView()
}
