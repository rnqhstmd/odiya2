import SwiftUI

struct ProfileView: View {

    @StateObject private var viewModel = ProfileViewModel()
    @State private var editingNickname = ""
    @State private var isEditingNickname = false

    var body: some View {
        NavigationStack {
            List {
                if let user = viewModel.user {
                    // 프로필 섹션
                    Section {
                        HStack(spacing: 16) {
                            AsyncImage(url: URL(string: user.profileImageUrl ?? "")) { image in
                                image
                                    .resizable()
                                    .scaledToFill()
                            } placeholder: {
                                Image(systemName: "person.circle.fill")
                                    .resizable()
                                    .foregroundStyle(.gray)
                            }
                            .frame(width: 64, height: 64)
                            .clipShape(Circle())

                            VStack(alignment: .leading, spacing: 4) {
                                Text(user.nickname)
                                    .font(.title3)
                                    .fontWeight(.semibold)
                            }
                        }
                        .padding(.vertical, 8)
                    }

                    // 닉네임 수정
                    Section("프로필 수정") {
                        HStack {
                            TextField("닉네임", text: $editingNickname)
                                .textFieldStyle(.plain)

                            Button("변경") {
                                viewModel.updateNickname(editingNickname)
                            }
                            .disabled(editingNickname.isEmpty)
                        }
                    }

                    // 계정 관리
                    Section("계정") {
                        Button("로그아웃") {
                            viewModel.logout()
                        }

                        Button("회원 탈퇴", role: .destructive) {
                            viewModel.showDeleteConfirmation = true
                        }
                    }
                }

                if let error = viewModel.errorMessage {
                    Section {
                        Text(error)
                            .foregroundStyle(.red)
                            .font(.footnote)
                    }
                }
            }
            .navigationTitle("내 프로필")
            .overlay {
                if viewModel.isLoading {
                    LoadingView()
                }
            }
            .alert("회원 탈퇴", isPresented: $viewModel.showDeleteConfirmation) {
                Button("탈퇴", role: .destructive) {
                    viewModel.deleteAccount()
                }
                Button("취소", role: .cancel) {}
            } message: {
                Text("정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")
            }
            .task {
                viewModel.loadProfile()
                if let user = viewModel.user {
                    editingNickname = user.nickname
                }
            }
            .onChange(of: viewModel.user) { newUser in
                if let user = newUser {
                    editingNickname = user.nickname
                }
            }
        }
    }
}

#Preview {
    ProfileView()
}
