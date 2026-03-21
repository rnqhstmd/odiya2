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
                        ForEach(viewModel.pendingRequests) { request in
                            HStack(spacing: 12) {
                                ProfileImageView(imageUrl: request.profileImageUrl, size: 44, nickname: request.nickname)

                                Text(request.nickname)
                                    .font(.body)

                                Spacer()

                                Button {
                                    viewModel.acceptRequest(request)
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
                                    viewModel.rejectRequest(request)
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
                    if viewModel.searchResults.isEmpty && !viewModel.searchText.isEmpty {
                        HStack {
                            Spacer()
                            Text("검색 결과가 없어요")
                                .font(.subheadline)
                                .foregroundStyle(.secondary)
                            Spacer()
                        }
                        .padding(.vertical, 20)
                    } else {
                        ForEach(viewModel.searchResults) { user in
                            HStack(spacing: 12) {
                                ProfileImageView(imageUrl: user.profileImageUrl, size: 44, nickname: user.nickname)

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

                // MARK: - QR 코드로 친구 추가
                Section {
                    VStack(spacing: 0) {
                        // "또는" 구분선
                        HStack(spacing: 12) {
                            Rectangle()
                                .fill(Color(.separator))
                                .frame(height: 1)
                            Text("또는")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                            Rectangle()
                                .fill(Color(.separator))
                                .frame(height: 1)
                        }
                        .padding(.vertical, 16)

                        // QR 아이콘 + 텍스트
                        VStack(spacing: 8) {
                            Image(systemName: "qrcode")
                                .font(.system(size: 48))
                                .foregroundStyle(OdiyaColors.primary)

                            Text("QR 코드로 친구 추가")
                                .font(.subheadline)
                                .fontWeight(.semibold)

                            Text("친구의 QR 코드를 스캔하거나 내 QR 코드를 보여주세요")
                                .font(.caption)
                                .foregroundStyle(.secondary)
                                .multilineTextAlignment(.center)
                        }
                        .padding(.vertical, 8)

                        // 버튼 2개
                        HStack(spacing: 12) {
                            Button {
                                // TODO: QR 스캔 화면 이동
                            } label: {
                                Text("스캔하기")
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                    .foregroundStyle(.white)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                                    .background(OdiyaColors.primary)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                            }
                            .buttonStyle(.plain)

                            Button {
                                // TODO: 내 QR 코드 표시 화면 이동
                            } label: {
                                Text("내 QR")
                                    .font(.subheadline)
                                    .fontWeight(.semibold)
                                    .foregroundStyle(OdiyaColors.primary)
                                    .frame(maxWidth: .infinity)
                                    .padding(.vertical, 12)
                                    .background(OdiyaColors.odiya100)
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                            }
                            .buttonStyle(.plain)
                        }
                        .padding(.top, 12)
                        .padding(.bottom, 4)
                    }
                }
            }
            .navigationTitle("친구 추가")
            .navigationBarTitleDisplayMode(.inline)
            .searchable(
                text: $viewModel.searchText,
                placement: .navigationBarDrawer(displayMode: .always),
                prompt: "닉네임으로 검색"
            )
            .onAppear {
                Task { await viewModel.loadPendingRequests() }
            }
            .onChange(of: viewModel.searchText) { _, newValue in
                Task { await viewModel.searchUsers() }
            }
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
