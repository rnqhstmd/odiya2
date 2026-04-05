import SwiftUI
import PhotosUI

private enum ProfileImageConstants {
    static let targetSize = CGSize(width: 1024, height: 1024)
    static let compressionQuality: CGFloat = 0.8
}

struct ProfileView: View {

    @StateObject private var viewModel = ProfileViewModel()
    @EnvironmentObject private var router: RootRouter
    @State private var editingNickname = ""
    @State private var isEditingNickname = false
    @State private var selectedPhotoItem: PhotosPickerItem?

    var body: some View {
        NavigationStack {
            List {
                if let user = viewModel.user {
                    // 프로필 섹션
                    Section {
                        HStack(spacing: 16) {
                            PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
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
                                .overlay(alignment: .bottomTrailing) {
                                    Image(systemName: "camera.fill")
                                        .font(.system(size: 12))
                                        .foregroundStyle(.white)
                                        .padding(4)
                                        .background(Color.accentColor)
                                        .clipShape(Circle())
                                }
                            }
                            .buttonStyle(.plain)

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
            .onAppear {
                viewModel.onLogout = { router.navigateToLogin() }
            }
            .task {
                viewModel.loadProfile()
                if let user = viewModel.user {
                    editingNickname = user.nickname
                }
            }
            .onChange(of: viewModel.user) { _, newUser in
                if let user = newUser {
                    editingNickname = user.nickname
                }
            }
            .onChange(of: selectedPhotoItem) { _, newItem in
                guard let newItem else { return }
                // 리사이즈/JPEG 인코딩은 UI 스레드를 막지 않도록 detached 태스크에서 수행.
                Task.detached(priority: .userInitiated) {
                    guard let data = try? await newItem.loadTransferable(type: Data.self),
                          let uiImage = UIImage(data: data) else {
                        await MainActor.run {
                            viewModel.errorMessage = "이미지를 불러올 수 없습니다."
                        }
                        return
                    }

                    let resized = uiImage.resizedToFill(size: ProfileImageConstants.targetSize)
                    guard let jpegData = resized.jpegData(
                        compressionQuality: ProfileImageConstants.compressionQuality
                    ) else {
                        await MainActor.run {
                            viewModel.errorMessage = "이미지 변환에 실패했습니다."
                        }
                        return
                    }

                    await MainActor.run {
                        viewModel.uploadProfileImage(jpegData)
                    }
                }
            }
        }
    }
}

#Preview {
    NavigationStack {
        ProfileView()
    }
    .environmentObject(RootRouter())
}
